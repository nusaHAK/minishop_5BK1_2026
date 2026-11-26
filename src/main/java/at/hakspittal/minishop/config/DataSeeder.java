package at.hakspittal.minishop.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import at.hakspittal.minishop.entity.Role;
import at.hakspittal.minishop.service.UserService;

/**
 * Initialisiert beim Start der Anwendung Standard-Benutzer in der Datenbank.
 * Wichtig: Da Admins nicht über das öffentliche Registrierungsformular erstellt werden dürfen,
 * wird hier beim ersten Start automatisch ein Admin-Account angelegt.
 */
@Component // Sorgt dafür, dass Spring diese Klasse beim Start erkennt und ausführt
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;

    // Konstruktor-Injektion des UserService
    public DataSeeder(UserService userService) {
        this.userService = userService;
    }

    /**
     * Die Methode run() wird von Spring Boot automatisch einmalig ausgeführt,
     * sobald die Anwendung vollständig hochgefahren ist.
     */
    @Override
    public void run(String... args) {
        // 1x Administrator anlegen (Zugriff auf /admin/**)
        userService.createUserIfNotExists(
            "admin",
            "admin@minishop.at",
            "admin123",
            Role.ADMIN
        );

        // 1x Test-Kunde anlegen (Zugriff auf Shop und Bestellungen)
        userService.createUserIfNotExists(
            "kunde",
            "kunde@minishop.at",
            "kunde123",
            Role.CUSTOMER
        );
    }
}