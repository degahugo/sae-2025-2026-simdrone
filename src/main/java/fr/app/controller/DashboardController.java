package fr.app.controller;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import fr.app.model.SimulationConfiguration;

public class DashboardController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label positionLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private Label depthLabel;

    @FXML
    private Label intensityLabel;

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

        positionLabel.setText(String.format("Position : x=%.2f | y=%.2f | z=%.2f", status.getX(), status.getY(), status.getZ()));
        speedLabel.setText(String.format("Vitesse : %.2f m/s", status.getSpeed()));
        stateLabel.setText("État : " + status.getState());
        temperatureLabel.setText(String.format("Température : %.1f °C", status.getTemperature()));
        depthLabel.setText(String.format("Profondeur : %.2f m", status.getDepth()));
        intensityLabel.setText(String.format("Intensité : %.3f", status.getIntensity()));
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
                client.sendConfiguration(configuration);
                client.sendStartSimulation(droneId);

                DroneStatus status = client.receiveStatus();
                updateTelemetry(status);

                logArea.appendText("Simulation lancée pour " + droneId + "\n");

            } catch (Exception e) {
                logArea.appendText("Erreur lancement simulation : " + e.getMessage() + "\n");
            }
        } else {
            logArea.appendText(" Client non connecté\n");
        }
    }


    private SimulationConfiguration configuration;

    public void setConfiguration(SimulationConfiguration configuration) {
        this.configuration = configuration;

        if (logArea != null && configuration != null) {
            logArea.appendText(
                    "Configuration : "
                            + configuration.getEnvironmentType()
                            + " | "
                            + configuration.getDroneType()
                            + " | "
                            + configuration.getSensors()
                            + "\n"
            );
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