package fr.app.controller;

import fr.app.model.EnvironmentType;
import fr.app.model.SensorType;
import fr.app.model.SimulationConfiguration;
import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class ConfigurationSimulationController {

    @FXML
    private ComboBox<EnvironmentType> environmentComboBox;

    @FXML
    private ComboBox<String> conditionComboBox;

    @FXML
    private ComboBox<SensorType> sensorComboBox;

    @FXML
    private Label statusLabel;

    private NetworkClient client;

    public void setClient(NetworkClient client) {
        this.client = client;
    }

    @FXML
    private void initialize() {

        environmentComboBox.getItems().addAll(EnvironmentType.values());

        conditionComboBox.getItems().addAll(
                "Aucune",
                "Courant faible",
                "Courant fort",
                "Turbulence",
                "Visibilité réduite"
        );

        sensorComboBox.getItems().addAll(SensorType.values());

        environmentComboBox.setValue(EnvironmentType.UNDERWATER_LINE);        conditionComboBox.setValue("Aucune");
        sensorComboBox.setValue(SensorType.CAMERA);
    }

    @FXML
    private void handleContinue() {

        EnvironmentType environment = environmentComboBox.getValue();
        String condition = conditionComboBox.getValue();
        SensorType sensor = sensorComboBox.getValue();

        if (environment == null || condition == null || sensor == null) {
            statusLabel.setText("Veuillez sélectionner tous les paramètres.");
            return;
        }

        try {
            SimulationConfiguration configuration = new SimulationConfiguration();
            configuration.setEnvironmentType(environment);
            configuration.setCondition(condition);
            configuration.setSensors(List.of(sensor));

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/SelectionDrone.fxml")
            );

            Scene scene = new Scene(loader.load());

            SelectionDroneController controller = loader.getController();
            controller.setClient(client);
            controller.setConfiguration(configuration);

            Stage stage = (Stage) environmentComboBox.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors du chargement de la sélection drone.");
        }
    }
}