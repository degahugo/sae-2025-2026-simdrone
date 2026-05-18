package fr.app.network;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

public class MainTestClient {

    public static void main(String[] args) {

        try {

            NetworkClient client = new NetworkClient("localhost", 62500, 62501);

            client.connect();

            client.sendCommand(PilotCommand.FORWARD);

            DroneStatus status = client.receiveStatus();

            System.out.println("Statut reçu : " + status.getState());

            client.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}