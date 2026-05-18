package fr.app.controller;

import fr.app.network.Client;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Classe permettant de récupérer l'adresse ip et le port
// Détermine les règles validité de l'adresse ip et le port

public class ConnexionController {

    // Variables récupérées du fichier FXML
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    // On garde le client pour pouvoir l’utiliser après (Dashboard / commandes)
    private Client client;

    @FXML
    private void ConnexionClique() {

        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        // Validation IPv4
        if (!isValidIpv4(ip)) {
            setStatus("Adresse IPv4 invalide.", "red");
            return;
        }

        // Validation port entre 49152 et 65535 (on choisira le port 62500)
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 49152 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Port invalide (doit être entre 49152 et 65535).", "red");
            return;
        }

        setStatus("Connexion en cours...", "orange");

        final int finalPort = port;

        // Connexion en arrière-plan pour ne pas bloquer l'interface (thread séparé)
        Task<Client> task = new Task<>() {
            @Override
            protected Client call() throws Exception {

                // Ce code s'exécute dans un thread séparé (pas sur JavaFX thread)
                // On crée le client TCP avec l'IP et le port saisis
                client = new Client(ip, finalPort);
                client.connect();

                return client;
            }
        };

        task.setOnSucceeded(e -> {

            setStatus("Connecté à " + ip + ":" + port, "green");

            try {
                // Chargement de la vue Selection de drone après connexion réussie
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/ConfigurationSimulation.fxml")
                );

                Parent root = loader.load();

                ConfigurationSimulationController controller = loader.getController();
                controller.setClient(client);
                // Récupération de la fenêtre actuelle et changement de scène
                Stage stage = (Stage) ipField.getScene().getWindow();
                stage.setScene(new Scene(root));

            } catch (Exception ex) {
                ex.printStackTrace();
                setStatus("Erreur chargement interface", "red");
            }
        });

        task.setOnFailed(e -> {

            Throwable ex = task.getException();
            ex.printStackTrace();

            setStatus("Échec : " + ex.getMessage(), "red");
        });

        new Thread(task).start();
    }

    // Vérification IPv4
    private boolean isValidIpv4(String ip) {

        // On vérifie que l'adresse IP est en 4 parties séparées par des points
        String[] parts = ip.split("\\.");

        if (parts.length != 4) return false;

        for (String part : parts) {
            try {
                int val = Integer.parseInt(part);

                // chaque partie doit être entre 0 et 255
                if (val < 0 || val > 255) return false;

            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    // Méthode utilitaire pour afficher le statut dans l'UI
    private void setStatus(String message, String color) {

        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + ";");
        statusLabel.setText(message);
    }
}