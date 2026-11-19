package at.hakspittal.minishop.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import at.hakspittal.minishop.entity.Product;
import at.hakspittal.minishop.model.CartItem;
import jakarta.servlet.http.HttpSession;

/**
 * Warenkorb, der pro Browser-Session im HttpSession-Objekt gehalten wird.
 * Es wird nichts in der Datenbank gespeichert - die Auswahl geht beim
 * Beenden der Session (bzw. nach dem Timeout) verloren.
 *
 * Die Liste liegt unter dem Attribut "cartItems" in der Session und
 * enthält Objekte vom Typ CartItem.
 */
@Service
public class CartService {

    //Name des Session-Attributs, unter dem die Warenkorbliste liegt
    public static final String SESSION_KEY = "cartItems";

    //Obergrenze pro Position, damit keine unsinnigen Mengen entstehen
    private static final int MAX_QUANTITY = 99;

    //gibt alle Positionen des Warenkorbs zurück (leere Liste, wenn noch nichts drin ist)
    @SuppressWarnings("unchecked")
    public List<CartItem> getItems(HttpSession session) {

        List<CartItem> items = (List<CartItem>) session.getAttribute(SESSION_KEY);

        if (items == null) {
            items = new ArrayList<>();
            session.setAttribute(SESSION_KEY, items);
        }

        return items;
    }

    //ein Produkt in den Warenkorb legen - ist es schon enthalten, wird nur die Menge erhöht
    public void add(HttpSession session, Product product, int quantity) {

        List<CartItem> items = getItems(session);

        for (CartItem item : items) {
            if (item.getProduct().getPid().equals(product.getPid())) {
                item.setQuantity(clamp(item.getQuantity() + quantity));
                return;
            }
        }

        items.add(new CartItem(product, clamp(quantity)));
    }

    //Menge einer Position ändern - bei Menge 0 oder darunter wird die Position entfernt
    public void updateQuantity(HttpSession session, Long pid, int quantity) {

        List<CartItem> items = getItems(session);
        Iterator<CartItem> iterator = items.iterator();

        while (iterator.hasNext()) {
            CartItem item = iterator.next();

            if (item.getProduct().getPid().equals(pid)) {
                if (quantity <= 0) {
                    iterator.remove();
                } else {
                    item.setQuantity(clamp(quantity));
                }
                return;
            }
        }
    }

    //eine Position komplett entfernen
    public void remove(HttpSession session, Long pid) {

        List<CartItem> items = getItems(session);
        items.removeIf(item -> item.getProduct().getPid().equals(pid));
    }

    //den gesamten Warenkorb leeren
    public void clear(HttpSession session) {
        getItems(session).clear();
    }

    //Anzahl aller Artikel (Summe der Mengen) - z.B. für "3 Artikel im Warenkorb"
    public int getItemCount(HttpSession session) {

        int count = 0;

        for (CartItem item : getItems(session)) {
            count += item.getQuantity();
        }

        return count;
    }

    //Gesamtpreis aller Positionen
    public float getTotal(HttpSession session) {

        float total = 0;

        for (CartItem item : getItems(session)) {
            total += item.getSubtotal();
        }

        return total;
    }

    //Menge immer zwischen 1 und MAX_QUANTITY halten
    private int clamp(int quantity) {
        return Math.min(MAX_QUANTITY, Math.max(1, quantity));
    }
}
