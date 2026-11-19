package at.hakspittal.minishop.model;

import at.hakspittal.minishop.entity.Product;

/**
 * Eine Position im Warenkorb: ein Produkt mit der gewählten Menge.
 * Das Objekt wird nicht in der Datenbank gespeichert, sondern liegt
 * temporär in der Browser-Session (siehe CartService).
 */
public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    //Summe dieser Position: Einzelpreis * Menge
    public float getSubtotal() {
        return product.getPrice() * quantity;
    }
}
