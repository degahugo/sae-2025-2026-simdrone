package fr.app.controller;

import fr.app.network.NetworkClient;
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
    private NetworkClient client;

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
        int portTcp;
        try {
            portTcp = Integer.parseInt(portText);
            if (portTcp < 49152 || portTcp > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Port invalide (doit être entre 49152 et 65535).", "red");
            return;
        }
        int portUdp = portTcp + 1;
        setStatus("Connexion en cours...", "orange");


        // Connexion en arrière-plan pour ne pas bloquer l'interface (thread séparé)
        Task<NetworkClient> task = new Task<>() {
            @Override
            protected NetworkClient call() throws Exception {

                // Ce code s'exécute dans un thread séparé (pas sur JavaFX thread)
                // On crée le client TCP avec l'IP et le port saisis
                NetworkClient networkClient = new NetworkClient(ip, portTcp, portUdp);
                networkClient.connect();

                return networkClient;
            }
        };

        task.setOnSucceeded(e -> {
            client = task.getValue();
            setStatus("Connecté à " + ip + ":" + portTcp, "green");

            try {
                // Chargement de la vue Dashboard après connexion réussie
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/Dashboard.fxml")
                );

                Parent root = loader.load();

                // 👉 On passe le client au controller du Dashboard (IMPORTANT)
                DashboardController controller = loader.getController();
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

/*package fr.app.controller;

import fr.app.network.NetworkClient;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnexionController {

    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    private NetworkClient client;

    @FXML
    private void ConnexionClique() {

        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        if (!isValidIpv4(ip)) {
            setStatus("Adresse IPv4 invalide.", "red");
            return;
        }

        int portTcp;
        try {
            portTcp = Integer.parseInt(portText);
            if (portTcp < 49152 || portTcp > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            setStatus("Port invalide (doit être entre 49152 et 65535).", "red");
            return;
        }

        int portUdp = portTcp + 1;

        setStatus("Connexion en cours...", "orange");

        Task<NetworkClient> task = new Task<>() {
            @Override
            protected NetworkClient call() throws Exception {

                NetworkClient networkClient =
                        new NetworkClient(ip, portTcp, portUdp);

                networkClient.connect();

                return networkClient;
            }
        };

        task.setOnSucceeded(e -> {

            client = task.getValue();

            setStatus("Connecté à " + ip + ":" + portTcp, "green");

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/Dashboard.fxml")
                );

                Parent root = loader.load();

                DashboardController controller = loader.getController();
                controller.setClient(client);

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
}*/