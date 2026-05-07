package fr.app.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
            System.out.println("[Serveur] Client connecté : " + clientSocket.getInetAddress());

            // Gérer chaque client dans un thread séparé
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Serveur] Reçu : " + line);
                writer.println("ACK: " + line); // réponse au client
            }
        } catch (IOException e) {
            System.out.println("[Serveur] Client déconnecté : " + e.getMessage());
        }
    }

    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}