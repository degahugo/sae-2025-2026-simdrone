package fr.app.network;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;

public class ClientReseau {
    public static void main(String[] args) {
        try {
            ClientReseau client = new ClientReseau();
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() throws Exception {

        // Connexion au serveur
        Socket socket = new Socket("localhost", 5000);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // Création du module de chiffrement
        ProtocoleDeChiffrement crypto = new ProtocoleDeChiffrement();

        // 1. Réception de la clé publique du serveur
        PublicKey clePublique = (PublicKey) in.readObject();

        // 2. Création + chiffrement de la clé AES
        byte[] cleAES = crypto.creerEtChiffrerCleAES(clePublique);

        // 3. Envoi de la clé AES chiffrée
        out.writeObject(cleAES);

        System.out.println("Connexion sécurisée établie côté client");

        socket.close();
    }
}
