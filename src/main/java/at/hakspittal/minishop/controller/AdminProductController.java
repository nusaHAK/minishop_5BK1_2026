package at.hakspittal.minishop.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import at.hakspittal.minishop.config.WebConfig;
import at.hakspittal.minishop.entity.Product;
import at.hakspittal.minishop.service.ProductService;
import jakarta.validation.Valid;


@RequestMapping("/admin/products")
@Controller
public class AdminProductController {

    //Erlaubte Bildformate fuer den Upload
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final ProductService product_service;

    AdminProductController(ProductService product_service) {
        this.product_service = product_service;
    }

    //Routen festlegen
    @GetMapping
    public String viewHomePage(Model model) {

        List<Product> listProducts = product_service.listAll();
        model.addAttribute("listProductsView", listProducts);

        return "admin/admin_products";  //Seitennamen werden immer als 
                         //String übergeben

    }

    //Route zum Erstellen eines neuen Produkts
    @GetMapping("/new")
    public String showNewProduct(Model model) {

        //Erstellen eines leeren Product-Obj., der anschließend über den View
        //befüllt werden soll. 
        Product product = new Product();
        model.addAttribute("productView", product);

        return "admin/admin_product_form";
    }

    //Route zum Speichern eines Produkts
    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("productView") Product product,
          BindingResult result,
          @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
          Model model) {

        //Bild nur übernehmen, wenn die übrigen Felder gültig sind
        if (!result.hasErrors() && imageFile != null && !imageFile.isEmpty()) {

            if (!ALLOWED_IMAGE_TYPES.contains(imageFile.getContentType())) {
                result.rejectValue("imageUrl", "upload.type",
                        "Bitte ein Bild im Format JPG, PNG, WEBP oder GIF wählen.");
            } else {
                try {
                    product.setImageUrl(storeImage(imageFile));
                } catch (IOException ex) {
                    result.rejectValue("imageUrl", "upload.failed",
                            "Das Bild konnte nicht gespeichert werden.");
                }
            }
        }

        if (result.hasErrors()){  //es ist ein Fehler aufgetreten - eine oder mehrere Angaben waren nicht korrekt - das Produkt kann so nicht gespeichert werden
            model.addAttribute("productView", product);  //verwende das Produkt-Objekt nochmal
                                                        //und gib das Objekt mit den bereits richtig ausgefüllten Daten nochmal an den View weiter.
            return "admin/admin_product_form";
        }
        //Speichern des neuen Produkts in der DB
        product_service.save(product);
        
        //Nach dem Speichern verlinken auf gewünschte Seite.
        return "redirect:/admin/products";
    }

    //Speichert das Bild mit zufälligem Dateinamen und liefert den Pfad für imageUrl.
    //Der Dateiname vom Client wird bewusst nicht verwendet (Schutz vor Pfad-Manipulation).
    private String storeImage(MultipartFile imageFile) throws IOException {

        String fileName = UUID.randomUUID() + extensionFor(imageFile.getContentType());

        Files.createDirectories(WebConfig.UPLOAD_DIR);

        try (InputStream in = imageFile.getInputStream()) {
            Files.copy(in, WebConfig.UPLOAD_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + fileName;
    }

    //Dateiendung passend zum geprüften Content-Type
    private String extensionFor(String contentType) {
        return switch (contentType == null ? "" : contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    //Route zum editieren eines bestimmten Produkts
    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = product_service.get(id);
        model.addAttribute("productView", product);

        return "admin/admin_product_form";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteProductForm(@PathVariable Long id) {
        product_service.delete(id);

        return "redirect:/admin/products";
    }
    
    

}
