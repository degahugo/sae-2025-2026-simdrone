package fr.app.network;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import fr.app.network.ProtocoleDeChiffrement;
import java.security.PublicKey;
import javax.crypto.SecretKey;
//Client TCP pour se connecter au serveur de simulation DronSim.

public class Client {

    private static final int TIMEOUT_MS = 5000;

    private final String adresse;
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    //  Ajout du protocole de chiffrement
    private ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();

    public Client(String adresse, int port) {
        this.adresse = adresse;
        this.port = port;
    }

     // Ouvre la connexion TCP vers le serveur.
     // IOException si la connexion échoue ou expire.

    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(adresse, port), TIMEOUT_MS);
        socket.setSoTimeout(TIMEOUT_MS);
        // Récupération de la date et heure actuelles
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
        String formattedDateTime = now.format(formatter);


        System.out.println("[Client] Connecté à " + adresse + ":" + port + " à la date : " + formattedDateTime);
        try {
            //  1. Réception de la clé publique RSA du serveur
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            PublicKey clePubliqueServeur = (PublicKey) ois.readObject();
            System.out.println("[Client] Clé publique reçue.");

            // 🔐 2. Génération de la clé AES
            SecretKey aesKey = protocole.generateAESKey();

            //  3. Chiffrement de la clé AES avec RSA
            byte[] aesChiffree = protocole.encryptAESKey(aesKey, clePubliqueServeur);

            //  4. Envoi de la clé AES chiffrée au serveur
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(aesChiffree);
            oos.flush();
            System.out.println("[Client] Clé AES chiffrée envoyée au serveur.");

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'échange RSA/AES", e);
        }

        // --- Communication texte classique ---
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);



        System.out.println("[Client] Prêt à communiquer à la date : " + formattedDateTime);
    }


    // Envoie un message texte au serveur.

    public void send(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    // Lit une ligne de réponse du serveur.

    public String receive() throws IOException {
        if (reader != null) {
            return reader.readLine();
        }
        return null;
    }

    // Fermeture de la connexion
    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("[Client] Déconnecté.");
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
