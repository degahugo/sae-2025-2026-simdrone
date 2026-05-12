package fr.app.network;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.network.ProtocoleDeChiffrement;
import fr.app.structure.JsonSerializer;
import fr.app.structure.SerialisationException;

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

    //Serialisation
    private JsonSerializer serializer = new JsonSerializer();

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

    //Envoie une commande de pilotage au serveur
    public void sendCommand(PilotCommand command) throws SerialisationException {
        CommandeMessage msg = new CommandeMessage(command);
        // on sérialise le message en le transformant en tableau de byte
        byte[] data = serializer.serialisation(msg);
        // Tableau de bytes
        //System.out.println("[Client] Bytes sérialisés     : " + java.util.Arrays.toString(data));

        // on transforme le tableau de byte en String correspondant au format JSON : {"command":"RIGHT"}
        String json = new String(data);
        System.out.println("[Client] Commande sérialisée : " + command + " -> " + json);
        writer.println(json);
    }

    //Reçoit le statut du drone envoyé par le serveur
    public DroneStatus receiveStatus() throws IOException, SerialisationException {
        String json = reader.readLine();
        if (json == null){
            return null;
        }
        return serializer.deserisation(json.getBytes(), DroneStatus.class);
    }
}
