package at.hakspittal.minishop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.hakspittal.minishop.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>{

    //stellt bereits geerbte Datenbankfunktionen zur Verfügung
    //hier können noch Erweiterungen vorgenommen werden

}
