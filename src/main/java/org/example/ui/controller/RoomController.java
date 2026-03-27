package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Room;
import org.example.model.enums.Role;
import org.example.service.RoomService;

import java.util.List;

public class RoomController {

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> nameColumn;
    @FXML private TableColumn<Room, Integer> capacityColumn;
    @FXML private HBox adminButtons;

    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        // Configure les colonnes
        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        capacityColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCapacity()));

        // Cache les boutons admin si pas admin
        boolean isAdmin = SessionManager.getCurrentUser() != null
                && SessionManager.getCurrentUser().getRole() == Role.ADMIN;
        adminButtons.setVisible(isAdmin);

        refreshRooms();
    }

    private void refreshRooms() {
        roomTable.getItems().clear();
        List<Room> rooms = roomService.getAllRooms();
        roomTable.getItems().addAll(rooms);
    }

    @FXML
    private void openCreateRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createRoom.fxml"));
            javafx.stage.Stage stage = new Stage();
            stage.setTitle("Ajouter une salle");
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.showAndWait();
            refreshRooms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        roomService.deleteRoom(selected.getId());
        refreshRooms();
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) roomTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
