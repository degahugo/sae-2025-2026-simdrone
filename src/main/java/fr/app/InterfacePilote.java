package fr.app;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class InterfacePilote extends Application {

    // Classe de lancement de l'interface de Pilotage afin d'afficher l'interface fxml correspondante
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InterfacePilote.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("DronSim - Connexion");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

