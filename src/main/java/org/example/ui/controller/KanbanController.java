package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class KanbanController {

    @FXML
    private ListView<String> todoList;

    @FXML
    private ListView<String> inProgressList;

    @FXML
    private ListView<String> doneList;

    @FXML
    public void initialize() {

        todoList.getItems().add("Créer base de données");
        inProgressList.getItems().add("Créer login");
        doneList.getItems().add("Initialiser projet");
    }
}