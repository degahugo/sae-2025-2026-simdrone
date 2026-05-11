import fr.app.network.ProtocoleDeChiffrement;

import java.security.PublicKey;
import javax.crypto.SecretKey;
import java.util.Base64;

public class TestUS5 {

    public static void main(String[] args) throws Exception {

        // --- Serveur ---
        ProtocoleDeChiffrement serveur = new ProtocoleDeChiffrement();
        serveur.generateRSAKeys(); // Génère RSA
        PublicKey clePublique = serveur.getPublicKey();

        // --- Client ---
        ProtocoleDeChiffrement client = new ProtocoleDeChiffrement();
        SecretKey cleAESClient = client.generateAESKey(); // Génère AES

        // Le client chiffre la clé AES avec RSA
        byte[] aesChiffree = client.encryptAESKey(cleAESClient, clePublique);

        // --- Serveur déchiffre ---
        serveur.decryptAESKey(aesChiffree);

        // Vérification
        byte[] clientKey = cleAESClient.getEncoded();
        byte[] serverKey = serveur.getAESKey().getEncoded();

        System.out.println("Clé AES générée par le client : " + Base64.getEncoder().encodeToString(clientKey));
        System.out.println("Clé AES reçue par le serveur : " + Base64.getEncoder().encodeToString(serverKey));

        boolean ok = Base64.getEncoder().encodeToString(clientKey)
                .equals(Base64.getEncoder().encodeToString(serverKey));

        System.out.println("\nRésultat du test US5 : " + (ok ? "OK ✔" : "ÉCHEC ❌"));
    }
}
