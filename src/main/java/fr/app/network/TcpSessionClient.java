package fr.app.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.SecretKey;

// Gère la session TCP côté client.
// Rôle : établir la connexion TCP avec le serveur, réaliser le handshake
// sécurisé (échange RSA/AES), puis envoyer le port UDP local au serveur.
// Une fois cette étape terminée, la communication des commandes
// se fait en UDP via UdpClientTransport.
public class TcpSessionClient {

    private static final int TIMEOUT_MS = 5000;

    private final String adresseServeur;
    private final int portTcp;

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    //Ajout du protocole de chiffrement
    private final ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();

    // clé AES
    private SecretKey aesKey;

    public TcpSessionClient(String adresseServeur, int portTcp) {
        this.adresseServeur = adresseServeur;
        this.portTcp = portTcp;
    }

    // Ouvre la connexion TCP vers le serveur.
    public void connect() throws Exception {
        socket = new Socket();
        socket.connect(new InetSocketAddress(adresseServeur, portTcp), TIMEOUT_MS);
        //socket.setSoTimeout(TIMEOUT_MS);
        // Récupération de la date et heure actuelles
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
        String formattedDateTime = now.format(formatter);

        System.out.println("[TCP Client] Connecté au serveur " + adresseServeur + ":" + portTcp+ " à la date : " + formattedDateTime);

        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.flush();

        objectInputStream = new ObjectInputStream(socket.getInputStream());

        faireHandshakeSecurise();
    }

    // Handshake sécurisé en 4 étapes :
    // 1. Réception de la clé publique RSA du serveur
    // 2. Génération d'une clé AES aléatoire côté client
    // 3. Chiffrement de la clé AES avec la clé publique RSA
    // 4. Envoi de la clé AES chiffrée au serveur
    private void faireHandshakeSecurise() throws Exception {
        //  1. Réception de la clé publique RSA du serveur
        PublicKey clePubliqueServeur = (PublicKey) objectInputStream.readObject();
        System.out.println("[TCP Client] Clé publique RSA reçue.");

        //  2. Génération de la clé AES
        aesKey = protocole.generateAESKey();
        System.out.println("[TCP Client] Clé AES générée.");

        //  3. Chiffrement de la clé AES avec RSA
        byte[] aesChiffree = protocole.encryptAESKey(aesKey, clePubliqueServeur);
        System.out.println("[TCP Client] Clé AES chiffrée (RSA) : "
                + java.util.Arrays.toString(aesChiffree).substring(0, 30) + "...");

        //  4. Envoi de la clé AES chiffrée au serveur
        objectOutputStream.writeObject(aesChiffree);
        objectOutputStream.flush();

        System.out.println("[TCP Client] Clé AES envoyée au serveur.");
    }

    // Envoie le port UDP local au serveur via TCP,
    // pour que le serveur sache où envoyer les réponses UDP
    public void sendUdpPort(int udpPort) throws Exception {
        objectOutputStream.writeObject(udpPort);
        objectOutputStream.flush();

        System.out.println("[TCP Client] Port UDP envoyé au serveur : " + udpPort);
        socket.setSoTimeout(0);
    }

    public String getAdresseServeur() {
        return adresseServeur;
    }

    public SecretKey getAesKey() {
        return aesKey;
    }

    public int getPortTcp() {
        return portTcp;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    //Ferme la connexion TCP
    public void disconnect() throws Exception {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("[TCP Client] Déconnecté.");
        }
    }
}