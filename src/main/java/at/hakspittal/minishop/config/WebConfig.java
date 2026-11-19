package at.hakspittal.minishop.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Liefert hochgeladene Produktbilder aus dem Ordner "uploads" unter /uploads/... aus.
// Bilder duerfen NICHT in src/main/resources/static liegen: dieser Ordner ist im
// fertigen Jar schreibgeschuetzt, Uploads wuerden dort beim Deploy verloren gehen.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Ordner "uploads" im Projektverzeichnis (wird beim ersten Upload angelegt)
    public static final Path UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Der Pfad muss mit "/" enden, sonst findet Spring die Dateien nicht
        String location = UPLOAD_DIR.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
