package fr.app.controller;


import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;

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

    @FXML
    private ImageView cameraView;   // zone d'affichage vidéo (Dashboard.fxml)


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

    //
      // Appelé par NetworkClient à chaque frame JPEG reçue en UDP.
     // Peut être appelé depuis n'importe quel thread — Platform.runLater pour le thread JavaFX.

    public void afficherFrameVideo(byte[] jpegBytes) {
        Platform.runLater(() -> {
            try {
                Image image = new Image(new ByteArrayInputStream(jpegBytes));
                cameraView.setImage(image);
            } catch (Exception e) {
                System.err.println("[Dashboard] Erreur affichage frame : " + e.getMessage());
            }
        });
    }

    private void send(PilotCommand cmd) {

        logArea.appendText("Commande : " + cmd + "\n");

        if (client == null || !client.isConnected()) {
            logArea.appendText("Client non connecté\n");
            return;
        }
        new Thread(() -> {
            try {
                client.sendCommand(droneId, cmd);
                DroneStatus status = client.receiveStatus();
                Platform.runLater(() -> updateTelemetry(status));

            } catch (Exception e) {
                Platform.runLater(() ->
                logArea.appendText("Erreur envoi : " + e.getMessage() + "\n"));
            }
        }, "cmd-" + cmd).start();
    }

    @FXML
    private void startSimulation() {

        if (client == null || !client.isConnected()) {
            logArea.appendText(" Client non connecté\n");
            return;
        }
        // Dans un thread séparé pour ne pas bloquer le thread JavaFX
        new Thread(() -> {
            try {
                client.sendStartSimulation(droneId);
                DroneStatus status = client.receiveStatus();
                Platform.runLater(() -> {
                    updateTelemetry(status);
                    logArea.appendText("Simulation lancée pour " + droneId + "\n");
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    logArea.appendText("Erreur lancement simulation : " + e.getMessage() + "\n"));
            }
        },"start-sim").start();
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