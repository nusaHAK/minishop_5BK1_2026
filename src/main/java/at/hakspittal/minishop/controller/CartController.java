package at.hakspittal.minishop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import at.hakspittal.minishop.entity.Product;
import at.hakspittal.minishop.model.CartItem;
import at.hakspittal.minishop.service.CartService;
import at.hakspittal.minishop.service.ProductService;
import jakarta.servlet.http.HttpSession;

/**
 * Kundenbereich: Produktliste, Produktdetails und Warenkorb.
 * Laut Projektvereinbarung liegen die Templates unter shop/.
 */
@Controller
public class CartController {

    private final ProductService product_service;
    private final CartService cart_service;

    CartController(ProductService product_service, CartService cart_service) {
        this.product_service = product_service;
        this.cart_service = cart_service;
    }

    // /shop - Liste aller Produkte
    @GetMapping("/shop")
    public String viewShop(Model model) {

        List<Product> listProducts = product_service.listAll();
        model.addAttribute("listProductsView", listProducts);

        return "shop/shop_list";
    }

    // /shop/{id} - Detailseite eines Produkts
    @GetMapping("/shop/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model) {

        Product product = product_service.find(id);

        //Produkt existiert nicht (mehr) - zurück zur Liste
        if (product == null) {
            return "redirect:/shop";
        }

        model.addAttribute("productView", product);

        return "shop/shop_detail";
    }

    // /cart - Warenkorb anzeigen
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {

        List<CartItem> items = cart_service.getItems(session);

        model.addAttribute("cartItemsView", items);
        model.addAttribute("cartCountView", cart_service.getItemCount(session));
        model.addAttribute("cartTotalView", cart_service.getTotal(session));

        return "shop/cart";
    }

    // "In den Warenkorb" - Produkt mit Menge in die Session legen
    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            HttpSession session) {

        Product product = product_service.find(id);

        if (product != null) {
            cart_service.add(session, product, quantity);
        }

        return "redirect:/cart";
    }

    //Menge einer Position ändern
    @PostMapping("/cart/update/{id}")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                                 HttpSession session) {

        cart_service.updateQuantity(session, id, quantity);

        return "redirect:/cart";
    }

    //eine Position entfernen
    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {

        cart_service.remove(session, id);

        return "redirect:/cart";
    }

    //kompletten Warenkorb leeren
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {

        cart_service.clear(session);

        return "redirect:/cart";
    }
}
