package fr.app.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;



public class ServeurReseau {

    public static void main(String[] args) {
        try {
            ServeurReseau serveur = new ServeurReseau();
            serveur.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void start() throws Exception {

        // Le serveur écoute sur le port 5000
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Serveur en attente...");

        // Attente d’un client
        Socket socket = serverSocket.accept();
        System.out.println("Client connecté");

        // Flux pour envoyer et recevoir des objets
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // Création du module de chiffrement
        ProtocoleDeChiffrement crypto = new ProtocoleDeChiffrement();

        // 1. Génération des clés RSA
        crypto.initServeur();

        // 2. Envoi de la clé publique au client
        out.writeObject(crypto.getClePublique());

        // 3. Réception de la clé AES chiffrée
        byte[] cleAES = (byte[]) in.readObject();

        // 4. Déchiffrement de la clé AES
        crypto.recevoirCleAES(cleAES);

        System.out.println("Connexion sécurisée établie côté serveur");

        socket.close();
        serverSocket.close();
    }
}
