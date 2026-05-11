package fr.app.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

// Test d'intégration : démarre un vrai serveur et vérifie que le client se connecte
// (Test qui vérifie que la connexion en local fonctionne pour vérifie la logique Client Serveur)
class ClientServeurTest {

    private static final int PORT_TEST = 62599;// test sur un port quelconque
    private Serveur serveur;
    private Thread serveurThread;

    @BeforeEach
    void demarrerServeur() {
        serveur = new Serveur(PORT_TEST);
        // On lance le serveur dans un thread pour ne pas bloquer le test
        serveurThread = new Thread(() -> {
            try {
                serveur.start();
            }
            catch (Exception ignored) {} // fermé proprement par stop()
        });
        serveurThread.setDaemon(true);
        serveurThread.start();

        // Petite pause pour laisser le serveur démarrer
        try {
            Thread.sleep(200);
        }
        catch (InterruptedException ignored) {}
    }

    @AfterEach
    void arreterServeur() throws Exception {
        serveur.stop();
    }

    @Test
    void clientSeConnecteAuServeur() {
        Client client = new Client("127.0.0.1", PORT_TEST);
        assertDoesNotThrow(client::connect);
        assertTrue(client.isConnected());
        assertDoesNotThrow(client::disconnect);
    }

    @Test
    void clientNePeutPasSeConnecterSiServeurAbsent() {
        // Aucun serveur sur ce port → doit lever une exception
        Client client = new Client("127.0.0.1", 62598);
        assertThrows(Exception.class, client::connect);
    }
}
