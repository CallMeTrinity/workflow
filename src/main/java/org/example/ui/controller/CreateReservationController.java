package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Room;
import org.example.service.ReservationService;
import org.example.service.RoomService;

import java.util.List;

public class CreateReservationController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField dateField;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private ComboBox<Room> roomBox;
    @FXML private Label errorLabel;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        List<Room> rooms = roomService.getAllRooms();
        roomBox.getItems().addAll(rooms);

        // Affiche le nom de la salle dans le ComboBox
        roomBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty || room == null ? null : room.getName());
            }
        });
        roomBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty || room == null ? null : room.getName());
            }
        });
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        String date = dateField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();
        Room selectedRoom = roomBox.getValue();

        if (title.isEmpty() || date.isEmpty() || startTime.isEmpty()
                || endTime.isEmpty() || selectedRoom == null) {
            errorLabel.setText("Tous les champs obligatoires doivent être remplis");
            return;
        }

        try {
            Long organizerId = SessionManager.getCurrentUser().getId();
            reservationService.createReservation(
                    title,
                    descriptionField.getText(),
                    date,
                    startTime,
                    endTime,
                    selectedRoom.getId(),
                    null,
                    organizerId
            );
            ((Stage) titleField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
