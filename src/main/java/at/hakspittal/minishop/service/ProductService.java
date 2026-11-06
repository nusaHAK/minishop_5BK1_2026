package at.hakspittal.minishop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import at.hakspittal.minishop.entity.Product;
import at.hakspittal.minishop.repository.ProductRepository;

@Service
public class ProductService {

    //Attribut, das auf das Repository des Produkts zeigt
    private final ProductRepository product_repo;

    ProductService(ProductRepository product_repo) {
        this.product_repo = product_repo;
    }

    //in der spitzen Klammer wird definiert,
    //welchen Typ die Elemente in der Liste haben sollen.
    //In unserem Fall werden in der Liste Objekte vom
    //Typ Product gespeichert.
    public List<Product> listAll(){
        return product_repo.findAll();
    }

    //ein neues Produkt in der DB speichern oder
    //ein geändertes Produkt speichern - Unterschied ergib sich aufgrund der pid, die mitgegeben wird,
    //sofern das Produkt nur geändert wurde. Neue Produkte haben noch keine pid. Diese wird von der
    //DB erst beim Insert gesetzt.
    public void save(Product product){
        product_repo.save(product);
    }

    //ein Produkt aus der DB laden
    public Product get(Long pid){
        return product_repo.findById(pid).get();
    }

    //löschen eines Produkts aus der DB
    public void delete(Long pid){
        product_repo.deleteById(pid);
    }

}
