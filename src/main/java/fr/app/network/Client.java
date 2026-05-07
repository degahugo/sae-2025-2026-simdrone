package fr.app.network;

import java.io.*;
import java.net.Socket;

//Client TCP pour se connecter au serveur de simulation DronSim.

public class Client {

    private static final int TIMEOUT_MS = 5000;

    private final String adresse;
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

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

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        System.out.println("[Client] Connecté à " + adresse + ":" + port);
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
