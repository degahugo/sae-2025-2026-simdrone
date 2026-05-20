package fr.app.network;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

public class MainClient {

    public static void main(String[] args) throws Exception {

        Client client = new Client("localhost", 62500);

        // Connexion + échange RSA/AES
        client.connect();

        // Envoi d’une commande test vers le drone1
        client.sendCommand("drone1", PilotCommand.FORWARD);

        // Réception du statut renvoyé par le serveur
        DroneStatus status = client.receiveStatus();

        System.out.println("[Client] Position reçue : " + status.getPositionText());
        System.out.println("[Client] Vitesse reçue : " + status.getSpeedText());
        System.out.println("[Client] État reçu : " + status.getState());

        client.disconnect();
    }
}