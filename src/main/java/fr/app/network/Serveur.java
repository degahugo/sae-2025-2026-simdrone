package fr.app.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import fr.app.network.ProtocoleDeChiffrement;
import java.security.PublicKey;


// Serveur TCP : Ordre des étapes :
// 1. Créer un ServerSocket sur un port donné (ex: 62500)
// 2. Attente en boucle de connexions entrantes (accept)
// 3. Pour chaque client connecté -> création d'un thread dédié
// 4. Lecture des messages du client et envoi d'un accusé de réception

public class Serveur {

    private final int port;
    private ServerSocket serverSocket;
    //  Ajout du protocole de chiffrement
    private ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();



    public Serveur(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("test " + serverSocket);
        System.out.println("[Serveur] En écoute sur le port " + port);
        //  Génération des clés RSA au démarrage du serveur
        try {
            protocole.generateRSAKeys();
            System.out.println("[Serveur] Clés RSA générées.");
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération RSA", e);
        }

        while (true) {
            Socket clientSocket = serverSocket.accept(); // bloquant

            // Récupération de la date et heure actuelles
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDateTime = now.format(formatter);

            System.out.println("[Serveur] Client connecté : " + clientSocket.getInetAddress()+ " à la date : " + formattedDateTime);

            // Gérer chaque client dans un thread séparé
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket socket) {
        try {
            //  Envoi de la clé publique RSA au client
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(protocole.getPublicKey());
            oos.flush();
            System.out.println("[Serveur] Clé publique envoyée au client.");

            //  Réception de la clé AES chiffrée
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            byte[] aesChiffree = (byte[]) ois.readObject();
            protocole.decryptAESKey(aesChiffree);
            System.out.println("[Serveur] Clé AES reçue et déchiffrée.");
            // Initialisation des flux binaires pour la communication chiffrée
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

            System.out.println("[Serveur] Flux binaires initialisés pour la communication chiffrée.");


            // --- Communication texte classique ---
            /*
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Serveur] Reçu : " + line);
                writer.println("ACK: " + line);
            }*/
            // Boucle de communication chiffrée AES/GCM
            // TODO: Remplacer définitivement la communication texte par la communication chiffrée

            while (true) {
                int ivLength = dataIn.readInt();
                byte[] iv = dataIn.readNBytes(ivLength);

                int dataLength = dataIn.readInt();
                byte[] encryptedData = dataIn.readNBytes(dataLength);

                byte[] plaintext = protocole.decryptAES(encryptedData, iv);
                String message = new String(plaintext);

                System.out.println("[Serveur] Message déchiffré : " + message);

                String response = "ACK: " + message;
                ProtocoleDeChiffrement.ChiffreAES chiffreReponse =
                        protocole.encryptAES(response.getBytes());

                dataOut.writeInt(chiffreReponse.iv.length);
                dataOut.write(chiffreReponse.iv);

                dataOut.writeInt(chiffreReponse.data.length);
                dataOut.write(chiffreReponse.data);

                dataOut.flush();
            }



        } catch (Exception e) {
            System.out.println("[Serveur] Client déconnecté : " + e.getMessage());
        }
    }






    // Arrête proprement le serveur en fermant le ServerSocket
    // Cela interrompt aussi le accept() bloquant dans start()
    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}