package fr.app.network;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

public class MainClient {

    public static void main(String[] args) throws Exception {

        Client client = new Client("localhost", 62500);

        // Connexion + échange RSA/AES
        client.connect();

        // Envoi d’un message test
        //client.send("Bonjour serveur !");
        client.sendCommand(PilotCommand.FORWARD);
        DroneStatus status = client.receiveStatus();
        String reponse = client.receive();

        System.out.println("[Client] Réponse du serveur : " + reponse);

        client.disconnect();
    }
}

