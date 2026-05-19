package fr.app.network;

import javax.crypto.SecretKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Serveur TCP : Ordre des étapes :
// 1. Créer un ServerSocket sur un port donné (ex: 62500)
// 2. Attente en boucle de connexions entrantes (accept)
// 3. Pour chaque client connecté -> création d'un thread dédié
// 4. Lecture des messages du client et envoi d'un accusé de réception

public class TcpSessionServeur implements Runnable {

    private final int portTcp;
    private ServerSocket serverSocket;
    //  Ajout du protocole de chiffrement
    private final ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();

    // re_oit le registre pour y déposer la clé AES après chaque handshake
    private final RegistreDesSessions registre;


    public TcpSessionServeur(int portTcp,RegistreDesSessions registre) {
        this.portTcp = portTcp;
        this.registre = registre;
    }


    @Override
    public void run() {
        try {
            start();
        } catch (Exception e) {
            System.out.println("[TCP Serveur] Erreur : " + e.getMessage());
        }
    }

    // Démarre le ServerSocket, génère les clés RSA,
    // puis boucle en attendant les connexions clients.
    public void start() throws Exception {
        serverSocket = new ServerSocket(portTcp);
        //  Génération des clés RSA au démarrage du serveur
        protocole.generateRSAKeys();

        System.out.println("[TCP Serveur] En écoute sur le port " + portTcp);
        System.out.println("[TCP Serveur] Clés RSA générées.");

        while (!serverSocket.isClosed()) {
            Socket clientSocket = serverSocket.accept(); // bloquant

            System.out.println("[TCP Serveur] Client connecté : "
                    + clientSocket.getInetAddress().getHostAddress());

            // Chaque client est géré dans un thread séparé
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    // Gère un client connecté :
    // 1. Envoi clé publique RSA -> client
    // 2. Réception clé AES chiffrée <- client
    // 3. Déchiffrement clé AES
    // 4. Réception du port UDP du client
    // 5. Maintien de la session TCP ouverte
    private void handleClient(Socket socket) {
        String ipClient = socket.getInetAddress().getHostAddress();
        // clé AES pour cette session : différente pour chaque client
        ProtocoleDeChiffrement protocoleSession = new ProtocoleDeChiffrement();
        protocoleSession.setRSAKeys(protocole.getPublicKey(), protocole.getPrivateKey());
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            //  Envoi de la clé publique RSA au client
            objectOutputStream.writeObject(protocole.getPublicKey());
            objectOutputStream.flush();
            System.out.println("[TCP Serveur] Clé publique envoyée.");

            // réception et déchiffrement clé AES
            byte[] aesChiffree = (byte[]) objectInputStream.readObject();
            protocoleSession.decryptAESKey(aesChiffree);
            SecretKey cleAes = protocoleSession.getAESKey();
            System.out.println("[TCP Serveur] Clé AES reçue et déchiffrée.");

            // récupération du port UDP du client
            int udpPortClient = (int) objectInputStream.readObject();
            System.out.println("[TCP Serveur] Port UDP client reçu : " + udpPortClient);
            System.out.println("[TCP Serveur] Adresse UDP client : "
                    + socket.getInetAddress().getHostAddress()
                    + ":" + udpPortClient);

            // dépot dans le registre pour que UDPServeurTransport puisse envoyer les réponseS
            registre.enregistrer(ipClient, udpPortClient, cleAes);

            // On maintient la connexion TCP ouverte
            while (!socket.isClosed()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.out.println("[TCP Serveur] Client déconnecté : " + e.getMessage());
        }finally {
            // Nettoyage de la session quand le client se déconnecte
            registre.supprimer(ipClient);
        }
    }
    // Fermer le ServerSocket
    public void stop() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}