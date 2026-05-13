package fr.app.network;

public class MainClient {

    public static void main(String[] args) throws Exception {

        Client client = new Client("localhost", 62500);

        // Connexion + échange RSA/AES
        client.connect();

        // Envoi d’un message test
        client.sendSecure("Bonjour serveur !");
        String reponse = client.receiveSecure();

        System.out.println("[Client] Réponse du serveur : " + reponse);

        client.disconnect();
    }
}

