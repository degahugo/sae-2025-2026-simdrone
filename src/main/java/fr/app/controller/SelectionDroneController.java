package fr.app.controller;

import fr.app.network.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
public class SelectionDroneController {

    @FXML
    private ComboBox<String> droneComboBox;

    private Client client;

    /*
     * Reçoit le client déjà connecté
     */
    public void setClient(Client client) {

        this.client = client;

        /*
         * Temporaire :
         * liste fixe des drones
         *
         * Plus tard :
         * les drones viendront du serveur
         */
        droneComboBox.getItems().addAll(
                "drone1",
                "drone2"
        );
    }

    @FXML
    private void handleSelectDrone() {

        String selectedDrone =
                droneComboBox.getValue();

        /*
         * Aucun drone sélectionné
         */
        if (selectedDrone == null) {
            return;
        }

        try {

            /*
             * Informe le serveur
             */
            client.send(
                    "SELECT;" + selectedDrone
            );

            /*
             * Chargement du dashboard
             */
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/Dashboard.fxml"
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            /*
             * Récupération du controller
             */
            DashboardController controller =
                    loader.getController();

            /*
             * Transmission :
             * - du client réseau
             * - du drone choisi
             */
            controller.setClient(client);

            controller.setDroneId(selectedDrone);

            /*
             * Changement de fenêtre
             */
            Stage stage =
                    (Stage) droneComboBox
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}