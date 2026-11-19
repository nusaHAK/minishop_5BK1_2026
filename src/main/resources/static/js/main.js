/* Kopfzeile: ganz oben transparent, ab dem ersten Scrollen weiß.
   Die Optik steckt in style.css (.navbar / .navbar.is-scrolled), hier wird nur
   die Klasse umgeschaltet. */
(function () {
    "use strict";

    var navbar = document.querySelector(".navbar");

    if (!navbar) {
        return;
    }

    // Ab dieser Scroll-Distanz wird die Leiste weiß
    var SCHWELLE = 4;

    function kopfzeileAktualisieren() {
        navbar.classList.toggle("is-scrolled", window.scrollY > SCHWELLE);
    }

    kopfzeileAktualisieren();

    window.addEventListener("scroll", kopfzeileAktualisieren, { passive: true });

    // Auch nach dem vollstaendigen Laden pruefen: Sprungmarken (z.B. /#vorteile)
    // und das Zurueckspringen aus dem Browser-Cache loesen nicht immer ein
    // scroll-Event aus.
    window.addEventListener("load", kopfzeileAktualisieren);
    window.addEventListener("hashchange", kopfzeileAktualisieren);
    window.addEventListener("pageshow", kopfzeileAktualisieren);
})();
