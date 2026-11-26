package at.hakspittal.minishop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import at.hakspittal.minishop.entity.User;
import at.hakspittal.minishop.service.UserService;

/**
 * Controller für Authentifizierung (Login & Registrierung).
 * Nimmt HTTP-Anfragen vom Browser entgegen und gibt passende HTML-Seiten zurück.
 */
@Controller
public class AuthController {

    private final UserService userService;

    // Konstruktor-Injektion des UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Zeigt die Login-Seite an (GET-Request auf /login).
     * Hinweis: Den eigentlichen Login-Vorgang (POST /login) übernimmt Spring Security automatisch.
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; // Verweist auf templates/auth/login.html
    }

    /**
     * Zeigt das leere Registrierungsformular an (GET-Request auf /register).
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // Ein leeres User-Objekt wird an das HTML-Formular übergeben,
        // damit Thymeleaf die Eingabefelder damit verknüpfen kann
        model.addAttribute("user", new User());
        return "auth/register"; // Verweist auf templates/auth/register.html
    }

    /**
     * Verarbeitet die abgeschickten Formulardaten (POST-Request auf /register).
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") User user, Model model) {
        // 1. Einfache Prüfung, ob alle Felder ausgefüllt wurden und das Passwort lang genug ist
        if (user.getUsername() == null || user.getUsername().isBlank() ||
            user.getEmail() == null || user.getEmail().isBlank() ||
            user.getPassword() == null || user.getPassword().length() < 4) {
            
            // Fehlermeldung ins Model legen und Formular erneut anzeigen
            model.addAttribute("errorMessage", "Bitte alle Felder ausfüllen (Passwort mindestens 4 Zeichen).");
            return "auth/register";
        }

        // 2. Registrierung über den UserService durchführen
        try {
            userService.registerCustomer(user);
            // Bei Erfolg: Weiterleitung auf /login mit URL-Parameter "?registered" für eine Erfolgsmeldung
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            // Falls Benutzername oder E-Mail schon vergeben sind, fangen wir die Exception ab
            // und zeigen die Fehlermeldung auf der Registrierungsseite an
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}