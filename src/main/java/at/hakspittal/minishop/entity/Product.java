package at.hakspittal.minishop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Product {

    //Attribute
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long pid;

    @NotBlank(message = "{NotBlank.product.name}")
    @Size(min = 2, max = 255, message = "{Size.product.name}")
    private String name;
    
    @NotBlank(message = "{NotBlank.product.brand}")
    @Size(min = 2, max = 255, message = "{Size.product.brand}")
    private String brand;

    @DecimalMin(value = "0.01", message = "{DecimalMin.product.price}")
    private float price;

    //Kurzbeschreibung fuer Karte und Detailseite (optional)
    @Size(max = 500, message = "{Size.product.description}")
    @Column(length = 500)
    private String description;

    //Relativer Pfad ab /static (z. B. /images/produkte/tasse.jpg) oder vollstaendige URL.
    //Optional: ohne Bild zeigt der Shop den Anfangsbuchstaben des Namens.
    @Size(max = 500, message = "{Size.product.imageUrl}")
    @Column(length = 500)
    private String imageUrl;
    
    //Standardkonstruktor
    public Product() {
        super();
    }

    //individuellen Konstruktor
    public Product(Long pid, String name, String brand, float price) {
        this.pid = pid;
        this.name = name;
        this.brand = brand;
        this.price = price;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    
    



}
