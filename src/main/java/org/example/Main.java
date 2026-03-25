package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.AuthService;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("ProjectRoom");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
