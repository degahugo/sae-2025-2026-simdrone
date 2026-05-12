package fr.app.controller;

import fr.app.model.PilotCommand;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import fr.app.network.Client;

public class DashboardController {

    @FXML
    private TextArea logArea;

    private void send(PilotCommand cmd) {

        logArea.appendText("Commande : " + cmd + "\n");

        if (client != null && client.isConnected()) {
            try{
                client.sendCommand(cmd);
            } catch (Exception e) {
                logArea.appendText(" Erreur envoi : " + e.getMessage() + "\n");
            }
            //client.send(cmd.name());   // 🔥 ENVOI AU SERVEUR
        } else {
            logArea.appendText("❌ Client non connecté\n");
        }
    }
    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }


    @FXML
    private void moveForward() { send(PilotCommand.FORWARD); }

    @FXML
    private void moveBackward() { send(PilotCommand.BACKWARD); }

    @FXML
    private void moveLeft() { send(PilotCommand.LEFT); }

    @FXML
    private void moveRight() { send(PilotCommand.RIGHT); }

    @FXML
    private void moveUp() { send(PilotCommand.UP); }

    @FXML
    private void moveDown() { send(PilotCommand.DOWN); }

    @FXML
    private void stopDrone() { send(PilotCommand.STOP); }
}