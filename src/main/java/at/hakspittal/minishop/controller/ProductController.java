package at.hakspittal.minishop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import at.hakspittal.minishop.entity.Product;
import at.hakspittal.minishop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/admin/products")
@Controller
public class ProductController {

    private final ProductService product_service;

    ProductController(ProductService product_service) {
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
          BindingResult result, Model model) {
       
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
