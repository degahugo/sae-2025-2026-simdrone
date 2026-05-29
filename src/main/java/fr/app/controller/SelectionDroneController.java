package fr.app.controller;

import fr.app.model.DroneType;
import fr.app.model.SimulationConfiguration;
import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.util.UUID;

public class SelectionDroneController {

    @FXML
    private ComboBox<DroneType> droneComboBox;

    private NetworkClient client;
    private SimulationConfiguration configuration;

    /*
     * Reçoit le client déjà connecté
     */
    public void setClient(NetworkClient client) {
        this.client = client;

        droneComboBox.getItems().clear();
        droneComboBox.getItems().addAll(DroneType.values());
        droneComboBox.setValue(DroneType.BR2R4);
    }

    public void setConfiguration(SimulationConfiguration configuration) {
        this.configuration = configuration;
    }

    @FXML
    private void handleSelectDrone() {

        DroneType selectedDrone = droneComboBox.getValue();

        /*
         * Aucun drone sélectionné
         */
        if (selectedDrone == null) {
            return;
        }

        try {
            if (configuration == null) {
                configuration = new SimulationConfiguration();
            }

            configuration.setDroneType(selectedDrone);

            //Générer un droneId unique pour ce client  évite les collisions multi-client
            String droneId = selectedDrone.getSimulatorName().toLowerCase()
                    + "_" + UUID.randomUUID().toString().substring(0, 8);

            //Mémoriser dans le NetworkClient pour les envois de commandes
            client.setDroneId(droneId);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml")
            );

            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.setClient(client);
            controller.setDroneId(droneId);
            controller.setConfiguration(configuration);

            Stage stage = (Stage) droneComboBox.getScene().getWindow();
            /*
             * Changement de fenêtre
             */
            try {
                client.demarrerReceptionVideo(controller::afficherFrameVideo);
                System.out.println("[SelectionDrone] Callback vidéo branché sur DashboardController");
            } catch (Exception e) {
                System.err.println("[SelectionDrone] Erreur démarrage réception vidéo : " + e.getMessage());
            }

            /*Stage stage =
                    (Stage) droneComboBox
                            .getScene()
                            .getWindow();*/



            // Arrête proprement le thread de polling et l'horloge quand
            // la fenêtre est fermée, pour éviter les fuites de thread.
            stage.setOnCloseRequest(e -> controller.shutdown());

            stage.setScene(scene);
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}