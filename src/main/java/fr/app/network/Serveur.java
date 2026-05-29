package fr.app.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.network.ProtocoleDeChiffrement;
import fr.app.structure.JsonSerializer;
import fr.app.model.MessageType;
import fr.utln.dronsim.viewer.example.WaterFilterExample;
import fr.app.model.DroneStatus;
import fr.app.Main;
import java.security.PublicKey;


// Serveur TCP : Ordre des étapes :
// 1. Créer un ServerSocket sur un port donné (ex: 62500)
// 2. Attente en boucle de connexions entrantes (accept)
// 3. Pour chaque client connecté -> création d'un thread dédié
// 4. Lecture des messages du client et envoi d'un accusé de réception

public class Serveur {

    private final int port;
    private ServerSocket serverSocket;
    private Main simulation;    //  Ajout du protocole de chiffrement
    private ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();

    // Serialisation
    private JsonSerializer serializer = new JsonSerializer();

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
        String clientIp = socket.getInetAddress().getHostAddress();
        try {
            //  Envoi de la clé publique RSA au client
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(protocole.getPublicKey());
            oos.flush();
            System.out.println("[Serveur] Clé publique envoyée au client.");

            //  Réception de la clé AES chiffrée
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            byte[] aesChiffree = (byte[]) ois.readObject();
            protocole.decryptAESKey(aesChiffree);
            System.out.println("[Serveur] Clé AES reçue et déchiffrée.");

            // --- Communication texte classique ---
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String line;

            /*while ((line = reader.readLine()) != null) {
                System.out.println("[Serveur] Reçu : " + line);
                writer.println("ACK: " + line);
            }*/
            while ((line = reader.readLine()) != null) {
                System.out.println("[Serveur] " + clientIp + " -> " + line);

                //Conversion String en bytes pour la désérialisation
                byte[] bytes = line.getBytes();
                System.out.println("[Serveur] Bytes reçus         : " + java.util.Arrays.toString(bytes));

                // Désérialisation : bytes vers objet Java (ici un enum de PilotCommand)
                CommandeMessage msg = serializer.deserisation(bytes, CommandeMessage.class);

                String droneId = msg.getDroneId();
                PilotCommand command = msg.getCommand();
                MessageType type = msg.getType();

                System.out.println("[Serveur] Type message       : " + type);
                System.out.println("[Serveur] Drone ciblé        : " + droneId);
                System.out.println("[Serveur] Commande reçue     : " + command);

                if (type == MessageType.START_SIMULATION) {

                    if (simulation == null) {

                        simulation = new Main();
                        com.jme3.system.AppSettings settings =
                                new com.jme3.system.AppSettings(true);

                        settings.setTitle("DronSim - Simulation 3D");
                        settings.setWidth(1024);
                        settings.setHeight(768);

                        simulation.setShowSettings(false);
                        simulation.setSettings(settings);

                        // IMPORTANT :
                        // la simulation continue même sans focus
                        simulation.setPauseOnLostFocus(false);

                        simulation.start();

                        System.out.println("[Serveur] Simulation lancée pour " + droneId);

                    } else {

                        System.out.println("[Serveur] Simulation déjà lancée.");
                    }
                }

                if (type == MessageType.COMMAND) {

                    if (simulation == null) {
                        System.out.println("[Serveur] Impossible d'appliquer la commande : simulation non lancée.");
                    } else {
                        simulation.enqueue(() -> {
                            simulation.executeNetworkCommand(droneId, command);
                            return null;
                        });
                    }
                }
                // Construit le statut du drone à renvoyer
                DroneStatus status;

                if (type == MessageType.START_SIMULATION) {

                    status = new DroneStatus();
                    status.setState("Simulation lancée");

                } else if (simulation != null) {

                    final DroneStatus[] statusHolder = new DroneStatus[1];

                    simulation.enqueue(() -> {
                        statusHolder[0] = simulation.getDroneStatusDirect(droneId);
                        return null;
                    }).get();

                    status = statusHolder[0];

                } else {

                    status = new DroneStatus();
                    status.setState("Simulation non lancée");
                }

                // Sérialise et envoie le statut au client : conversion en Json
                byte[] data = serializer.serialisation(status);
                String jsonStatus = new String(data);
                System.out.println("[Serveur] Statut sérialisé envoyé : " + jsonStatus);
                //System.out.println("[Serveur] Bytes statut envoyé : " + java.util.Arrays.toString(data));

                writer.println(jsonStatus);
            }

        } catch (Exception e) {
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