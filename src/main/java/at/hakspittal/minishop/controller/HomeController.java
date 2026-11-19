package at.hakspittal.minishop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    //Startseite des Kunden-Shops
    @GetMapping("/")
    public String viewHomePage() {
        return "home/home";  //Template: templates/home/home.html
    }
}
