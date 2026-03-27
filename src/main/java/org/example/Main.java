package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Icône de la fenêtre / taskbar
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/images/workflow_logo.png"))
        );

        // Démarre sur le splash screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/splash.fxml"));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm()
        );

        stage.setTitle("WorkFlow");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}