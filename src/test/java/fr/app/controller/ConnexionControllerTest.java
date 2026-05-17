package fr.app.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Tests unitaires de la logique de validation
class ConnexionControllerTest {

    // On test uniquement les méthodes de validité de l'adresse IP et du port

    static boolean isValidIpv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String part : parts) {
            try {
                int val = Integer.parseInt(part);
                if (val < 0 || val > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    static boolean isValidPort(String portText) {
        try {
            int port = Integer.parseInt(portText);
            return port >= 49152 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // --- Tests IPv4 valides ---

    @Test
    void ipValideStandard() {
        assertTrue(isValidIpv4("192.168.1.1"));
    }

    @Test
    void ipValideAvecZeros() {
        assertTrue(isValidIpv4("0.0.0.0"));
    }

    @Test
    void ipValideMax() {
        assertTrue(isValidIpv4("255.255.255.255"));
    }

    // --- Tests IPv4 invalides ---

    @Test
    void ipInvalide_valeurSupA255() {
        assertFalse(isValidIpv4("256.0.0.1"));
    }

    @Test
    void ipInvalide_tropDeParts() {
        assertFalse(isValidIpv4("192.168.1.1.1"));
    }

    @Test
    void ipInvalide_pasDeParts() {
        assertFalse(isValidIpv4("192168"));
    }

    @Test
    void ipInvalide_lettres() {
        assertFalse(isValidIpv4("abc.def.ghi.jkl"));
    }

    @Test
    void ipInvalide_vide() {
        assertFalse(isValidIpv4(""));
    }

    // --- Tests port valides ---

    @Test
    void portValide() {
        assertTrue(isValidPort("62500"));
    }

    @Test
    void portValide_limiteMin() {
        assertTrue(isValidPort("49152"));
    }

    @Test
    void portValide_limiteMax() {
        assertTrue(isValidPort("65535"));
    }

    // --- Tests port invalides ---

    @Test
    void portInvalide_tropPetit() {
        assertFalse(isValidPort("1024"));
    }

    @Test
    void portInvalide_tropGrand() {
        assertFalse(isValidPort("65536"));
    }

    @Test
    void portInvalide_lettres() {
        assertFalse(isValidPort("abc"));
    }

    @Test
    void portInvalide_vide() {
        assertFalse(isValidPort(""));
    }
}
