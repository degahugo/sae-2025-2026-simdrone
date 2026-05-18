package fr.app.controller;

import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfigurationSimulationController {

    @FXML
    private ComboBox<String> environmentComboBox;

    @FXML
    private ComboBox<String> conditionComboBox;

    @FXML
    private ComboBox<String> sensorComboBox;

    @FXML
    private Label statusLabel;

    private NetworkClient client;

    public void setClient(NetworkClient client) {
        this.client = client;
    }

    @FXML
    private void initialize() {

        environmentComboBox.getItems().addAll(
                "Militaire",
                "Sous-marin"
        );

        conditionComboBox.getItems().addAll(
                "Aucune",
                "Vent",
                "Courant"
        );

        sensorComboBox.getItems().addAll(
                "Caméra"
        );

        environmentComboBox.setValue("Sous-marin");
        conditionComboBox.setValue("Aucune");
        sensorComboBox.setValue("Caméra");
    }

    @FXML
    private void handleContinue() {

        String environment = environmentComboBox.getValue();
        String condition = conditionComboBox.getValue();
        String sensor = sensorComboBox.getValue();

        if (environment == null || condition == null || sensor == null) {
            statusLabel.setText("Veuillez sélectionner tous les paramètres.");
            return;
        }

        try {
            /*
             * Pour l'instant, on garde seulement la configuration côté interface.
             * Plus tard, on l'enverra au serveur avec un message JSON structuré.
             */
            System.out.println("[Configuration] Environnement : " + environment);
            System.out.println("[Configuration] Condition : " + condition);
            System.out.println("[Configuration] Capteur : " + sensor);

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/SelectionDrone.fxml"
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            SelectionDroneController controller =
                    loader.getController();

            controller.setClient(client);

            Stage stage =
                    (Stage) environmentComboBox
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors du chargement de la sélection drone.");
        }
    }
}