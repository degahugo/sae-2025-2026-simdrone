package fr.app.controller;

import fr.app.model.environment.EnvironmentConditions;
import fr.app.model.environment.EnvironmentType;
import fr.app.model.SensorType;
import fr.app.model.SimulationConfiguration;
import fr.app.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class ConfigurationSimulationController {

    @FXML
    private ComboBox<EnvironmentType> environmentComboBox;

    @FXML
    private ComboBox<EnvironmentConditions> conditionComboBox;

    @FXML
    private ComboBox<EnvironmentConditions> visibilityComboBox;

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
        // Traductions des comboBox
        configureComboBoxConverters();

        environmentComboBox.getItems().addAll(EnvironmentType.values());
        environmentComboBox.setValue(EnvironmentType.UNDERWATER_LINE);

        // Initialise les conditions pour la première fois selon la valeur par défaut
        updateConditions(environmentComboBox.getValue());

        // LISTENER : Change la liste selon l'environnement choisit
        environmentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateConditions(newValue);
        });

        sensorComboBox.getItems().addAll(SensorType.values());
        sensorComboBox.setValue(SensorType.CAMERA);

    }

    /**
     * AJOUT : Définit la manière dont les énumérations sont affichées à l'écran
     */
    private void configureComboBoxConverters() {
        // Convertisseur pour le courant
        conditionComboBox.setConverter(new StringConverter<EnvironmentConditions>() {
            @Override
            public String toString(EnvironmentConditions condition) {
                if (condition == null) return "";

                // Traduction de l'enum
                return switch (condition) {
                    case NO_CURRENT -> "Aucun courant";
                    case LIGHT_CURRENT -> "Courant faible";
                    case NORMAL_CURRENT -> "Courant normal";
                    case STRONG_CURRENT -> "Courant fort";
                    case VERY_STRONG_CURRENT -> "Courant très fort";
                    default -> condition.toString(); // Sécurité au cas où
                };
            }

            @Override
            public EnvironmentConditions fromString(String string) {
                // ComboBox n'est pas éditable au clavier
                return null;
            }
        });

        // Convertisseur pour la visibilité
        visibilityComboBox.setConverter(new StringConverter<EnvironmentConditions>() {
            @Override
            public String toString(EnvironmentConditions visibility) {
                if (visibility == null) return "";

                // Traduction de l'enum
                return switch (visibility) {
                    case VERY_GOOD_VISIBILITY -> "Très bonne visibilité";
                    case GOOD_VISIBILITY -> "Bonne visibilité";
                    case CORRECT_VISIBILITY -> "Visibilité correcte";
                    case BAD_VISIBILITY -> "Mauvaise visibilité";
                    case VERY_BAD_VISIBILITY -> "Très mauvaise visibilité";
                    case NO_VISIBILITY -> "Visibilité nulle";
                    default -> visibility.toString();
                };
            }

            @Override
            public EnvironmentConditions fromString(String string) {
                // ComboBox n'est pas éditable au clavier
                return null;
            }
        });
    }

    private void updateConditions(EnvironmentType environment) {
        // On reset l'ancienne sélection suite au changement d'environnement
        // On peut mettre une vérification si on reste sur du sous marin pour ne pas les reset mais flemme
        conditionComboBox.getItems().clear();
        visibilityComboBox.getItems().clear();
        if (environment == null) {
            conditionComboBox.setValue(null);
            visibilityComboBox.setValue(null);
            return;
        }
        // On crée la liste en fonction de l'environnement choisi
        switch (environment) {
            // Ajouter les conditions terrestres si nécessaire ici

            // Conditions sous marines
            case POINT_BORLO:
            case UNDERWATER_SPIRAL:
            case UNDERWATER_LINE:
            default:
                conditionComboBox.getItems().addAll(
                        EnvironmentConditions.NO_CURRENT,
                        EnvironmentConditions.LIGHT_CURRENT,
                        EnvironmentConditions.NORMAL_CURRENT,
                        EnvironmentConditions.STRONG_CURRENT,
                        EnvironmentConditions.VERY_STRONG_CURRENT
                );
                visibilityComboBox.getItems().addAll(
                        EnvironmentConditions.VERY_GOOD_VISIBILITY,
                        EnvironmentConditions.GOOD_VISIBILITY,
                        EnvironmentConditions.CORRECT_VISIBILITY,
                        EnvironmentConditions.BAD_VISIBILITY,
                        EnvironmentConditions.VERY_BAD_VISIBILITY,
                        EnvironmentConditions.NO_VISIBILITY
                );
                conditionComboBox.setValue(EnvironmentConditions.NO_CURRENT);
                visibilityComboBox.setValue(EnvironmentConditions.VERY_GOOD_VISIBILITY);
                break;
        }
    }

    @FXML
    private void handleContinue() {

        EnvironmentType environment = environmentComboBox.getValue();
        EnvironmentConditions condition = conditionComboBox.getValue();
        EnvironmentConditions visibility = visibilityComboBox.getValue();
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