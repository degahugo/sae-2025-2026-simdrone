package fr.app.controller;

import fr.app.network.Client;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

// Classe permettant de récupérer l'adresse ip et le port

public class ConnexionController {

    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    @FXML
    private void ConnexionClique() {
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        // Validation IPv4
        if (!isValidIpv4(ip)) {
            setStatus("Adresse IPv4 invalide.", "red");
            return;
        }

        // Validation port
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Port invalide (doit être entre 1 et 65535).", "red");
            return;
        }

        setStatus("Connexion en cours...", "orange");

        // Connexion en arrière-plan pour ne pas bloquer l'interface
        final int finalPort = port;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Client client = new Client(ip, finalPort);
                client.connect();
                return null;
            }
        };

        task.setOnSucceeded(e -> setStatus("Connecté à " + ip + ":" + port, "green"));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            setStatus("Échec : " + ex.getMessage(), "red");
        });

        new Thread(task).start();
    }

    private boolean isValidIpv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String part : parts) {
            try {
                int val = Integer.parseInt(part);
                if (val < 0 || val > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void setStatus(String message, String color) {
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + ";");
        statusLabel.setText(message);
    }
}
