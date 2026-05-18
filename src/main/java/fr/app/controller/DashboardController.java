package fr.app.controller;

import fr.app.model.PilotCommand;
import fr.app.network.Client;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class DashboardController {

    @FXML
    private TextArea logArea;

    private Client client;
    private String droneId;

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void send(PilotCommand cmd) {

        logArea.appendText("Commande : " + cmd + "\n");

        if (client != null && client.isConnected()) {
            try {
                client.sendCommand(droneId,cmd);
            } catch (Exception e) {
                logArea.appendText(" Erreur envoi : " + e.getMessage() + "\n");
            }
        } else {
            logArea.appendText("❌ Client non connecté\n");
        }
    }

    @FXML
    private void startSimulation() {

        if (client != null && client.isConnected()) {
            try {
                client.sendStartSimulation(droneId);
                logArea.appendText("Simulation lancée pour " + droneId + "\n");
            } catch (Exception e) {
                logArea.appendText("Erreur lancement simulation : " + e.getMessage() + "\n");
            }
        } else {
            logArea.appendText("❌ Client non connecté\n");
        }
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