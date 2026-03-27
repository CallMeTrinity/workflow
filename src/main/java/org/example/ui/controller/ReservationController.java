package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.service.ReservationService;
import org.example.service.RoomService;

import java.util.List;

public class ReservationController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> titleColumn;
    @FXML private TableColumn<Reservation, String> dateColumn;
    @FXML private TableColumn<Reservation, String> startColumn;
    @FXML private TableColumn<Reservation, String> endColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        dateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        startColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStartTime()));
        endColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEndTime()));
        roomColumn.setCellValueFactory(data -> {
            try {
                Room room = roomService.getRoomById(data.getValue().getRoomId());
                return new javafx.beans.property.SimpleStringProperty(room.getName());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Inconnue");
            }
        });

        refreshReservations();
    }

    private void refreshReservations() {
        reservationTable.getItems().clear();
        List<Reservation> reservations = reservationService.getAllReservations();
        reservationTable.getItems().addAll(reservations);
    }

    @FXML
    private void openCreateReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createReservation.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nouvelle réservation");
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.showAndWait();
            refreshReservations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelReservation() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            reservationService.cancelReservation(selected.getId());
            refreshReservations();
        } catch (Exception e) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR, e.getMessage()
            ).show();
        }
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) reservationTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
