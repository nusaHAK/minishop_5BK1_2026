package at.hakspittal.minishop.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import at.hakspittal.minishop.entity.Product;
import at.hakspittal.minishop.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@RequestMapping("/shop")
public class ShopController {
    private final ProductRepository productRepository;

    public ShopController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String showShopList(Model model) {
        // Ins Modell packen, damit thymeleaf darauf zugreifen kann
        model.addAttribute("products", productRepository.findAll());

        return "shop/shop_list";
    }

    // Route zur Detailseite eines Produktes über die ID
    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        Optional<Product> product = productRepository.findById(id);

        // Bei leerer ID zurück zur Shop-Seite, statt Fehlermeldung
        if (product.isEmpty()) {
            return "redirect:/shop";
        }

        model.addAttribute("product", product.get());

        return "shop/shop_detail";
    }
    

}
