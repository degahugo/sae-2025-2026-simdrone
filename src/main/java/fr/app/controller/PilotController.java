package fr.app.controller;

import fr.app.model.PilotCommand;
import fr.app.network.Client;

public class PilotController {

    private Client client;
    private boolean connected = false;

    private String droneId;

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    // Connexion réelle au serveur
    public boolean connect(String ip, String port) {

        try {
            int p = Integer.parseInt(port);

            client = new Client(ip, p);
            client.connect();

            connected = true;

            System.out.println("Connecté à " + ip + ":" + port);
            return true;

        } catch (Exception e) {
            connected = false;
            System.out.println("Erreur connexion : " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (client != null) {
                client.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        connected = false;
        System.out.println("Déconnexion demandée");
    }

    // Envoi de commande drone
    public void sendCommand(PilotCommand command) {

        if (!connected || client == null) {
            System.out.println("Impossible d'envoyer : non connecté");
            return;
        }

        if (droneId == null || droneId.isBlank()) {
            System.out.println("Impossible d'envoyer : aucun drone sélectionné");
            return;
        }

        try {
            // Envoie la commande sérialisée en JSON :
            // {"type":"COMMAND","droneId":"drone1","command":"RIGHT"}
            client.sendCommand(droneId, command);

        } catch (Exception e) {
            System.out.println("Erreur envoi commande : " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }
}