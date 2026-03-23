package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class DashboardController {

    @FXML
    private ListView<String> projectList;

    @FXML
    public void initialize() {

        // Test visuel (temporaire)
        projectList.getItems().add("Projet 1");
        projectList.getItems().add("Projet 2");
    }
}