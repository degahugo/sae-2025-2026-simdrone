package fr.app.controller;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import fr.app.network.NetworkClient;

public class DashboardController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label positionLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private Label stateLabel;

    @FXML
    private Label temperatureLabel;

    private NetworkClient client;
    private String droneId;

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public void setClient(NetworkClient client) {
        this.client = client;
    }
    private void updateTelemetry(DroneStatus status) {

        if (status == null) {
            return;
        }

        positionLabel.setText("Position : " + status.getPositionText());
        speedLabel.setText("Vitesse : " + status.getSpeedText());
        stateLabel.setText("État : " + status.getState());
        temperatureLabel.setText("Température : " + status.getTemperatureText());
    }

    private void send(PilotCommand cmd) {

        logArea.appendText("Commande : " + cmd + "\n");

        if (client != null && client.isConnected()) {
            try {
                client.sendCommand(droneId, cmd);

                DroneStatus status = client.receiveStatus();
                updateTelemetry(status);

            } catch (Exception e) {
                logArea.appendText("Erreur envoi : " + e.getMessage() + "\n");
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

                DroneStatus status = client.receiveStatus();
                updateTelemetry(status);

                logArea.appendText("Simulation lancée pour " + droneId + "\n");

            } catch (Exception e) {
                logArea.appendText("Erreur lancement simulation : " + e.getMessage() + "\n");
            }
        } else {
            logArea.appendText("❌ Client non connecté\n");
        }
    }

    @FXML
    private void moveForward() {
        send(PilotCommand.FORWARD);
    }

    @FXML
    private void moveBackward() {
        send(PilotCommand.BACKWARD);
    }

    @FXML
    private void moveLeft() {
        send(PilotCommand.LEFT);
    }

    @FXML
    private void moveRight() {
        send(PilotCommand.RIGHT);
    }

    @FXML
    private void moveUp() {
        send(PilotCommand.UP);
    }

    @FXML
    private void moveDown() {
        send(PilotCommand.DOWN);
    }

    @FXML
    private void stopDrone() {
        send(PilotCommand.STOP);
    }
}