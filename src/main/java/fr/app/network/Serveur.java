package fr.app.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Serveur TCP : Ordre des étapes :
// 1. Créer un ServerSocket sur un port donné (ex: 62500)
// 2. Attente en boucle de connexions entrantes (accept)
// 3. Pour chaque client connecté -> création d'un thread dédié
// 4. Lecture des messages du client et envoi d'un accusé de réception

public class Serveur {

    private final int port;
    private ServerSocket serverSocket;

    public Serveur(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("test " + serverSocket);
        System.out.println("[Serveur] En écoute sur le port " + port);


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
        String clientIp = socket.getInetAddress().getHostAddress();
        try (
                // BufferedReader : lit les données envoyées par le client (texte ligne par ligne)
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // PrintWriter : envoie des données au client (autoFlush=true -> envoi immédiat)
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Serveur] " + clientIp + " -> " + line);                // On renvoie un accusé de réception au client pour confirmer la réception
                writer.println("ACK: " + line); // réponse au client
            }
        } catch (IOException e) {
            System.out.println("[Serveur] Client déconnecté (" + clientIp + ") : " + e.getMessage());        }
    }

    // Arrête proprement le serveur en fermant le ServerSocket
    // Cela interrompt aussi le accept() bloquant dans start()
    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}