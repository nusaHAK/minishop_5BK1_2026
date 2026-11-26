package at.hakspittal.minishop.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import at.hakspittal.minishop.entity.Role;
import at.hakspittal.minishop.entity.User;
import at.hakspittal.minishop.repository.UserRepository;

/**
 * Service-Klasse für die Benutzerverwaltung.
 * Hier liegt die Geschäftslogik für das Registrieren und Anlegen neuer Benutzer.
 */
@Service 
public class UserService {

    // Repository für den Datenbankzugriff auf die Tabelle "users"
    private final UserRepository userRepository;

    // PasswordEncoder (BCrypt) aus der SecurityConfig zum sicheren Verschlüsseln von Passwörtern
    private final PasswordEncoder passwordEncoder;

    // Konstruktor-Injektion: Spring übergibt Repository und PasswordEncoder automatisch beim Start
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registriert einen neuen Kunden über das Registrierungsformular.
     * Prüft auf Duplikate, verschlüsselt das Passwort und vergibt die Rolle CUSTOMER.
     */
    public void registerCustomer(User user) {
        // 1. Prüfen, ob der Benutzername in der Datenbank schon existiert
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Dieser Benutzername ist bereits vergeben.");
        }

        // 2. Prüfen, ob die E-Mail-Adresse in der Datenbank schon existiert
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Diese E-Mail-Adresse wird bereits verwendet.");
        }

        // 3. Passwort aus Sicherheitsgründen mit BCrypt hashen (nie im Klartext speichern!)
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // 4. Standardwerte setzen: Neue Nutzer über /register sind immer Kunden (CUSTOMER) und aktiv
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        // 5. Fertiges User-Objekt in der Datenbank speichern
        userRepository.save(user);
    }

    /**
     * Hilfsmethode für den DataSeeder beim Programmstart:
     * Legt einen Benutzer (z. B. ADMIN oder Test-CUSTOMER) nur dann an,
     * wenn Benutzername und E-Mail noch nicht in der Datenbank vorhanden sind.
     */
    public void createUserIfNotExists(String username, String email, String rawPassword, Role role) {
        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            // Auch Start-Passwörter müssen verschlüsselt werden, sonst funktioniert der Login nicht
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEnabled(true);

            userRepository.save(user);
        }
    }
}
