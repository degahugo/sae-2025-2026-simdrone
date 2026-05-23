package fr.app.network;

/**
 * Point d'entrée du serveur pour les tests.
 *
 * Ports :
 *  62500 → TCP  (connexion + handshake)
 *  62501 → UDP  (commandes pilotage + statut drone)
 *  62502 → UDP  (streaming vidéo frames JPEG)
 */

public class MainTestServeur {

    public static void main(String[] args) {
        try {

            NetworkServeur server = new NetworkServeur(62500, 62501,62502);
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
