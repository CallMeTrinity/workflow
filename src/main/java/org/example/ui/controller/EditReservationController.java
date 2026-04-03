package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditReservationController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> startTimeBox;
    @FXML private ComboBox<String> endTimeBox;
    @FXML private ComboBox<Room> roomBox;
    @FXML private ListView<User> participantList;
    @FXML private Label errorLabel;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();
    private final UserService userService = new UserService();

    private Reservation reservation;

    @FXML
    public void initialize() {
        // Time slots (30 min intervals, 08:00 - 20:00)
        for (int h = 8; h <= 20; h++) {
            startTimeBox.getItems().add(String.format("%02d:00", h));
            endTimeBox.getItems().add(String.format("%02d:00", h));
            if (h < 20) {
                startTimeBox.getItems().add(String.format("%02d:30", h));
                endTimeBox.getItems().add(String.format("%02d:30", h));
            }
        }
        startTimeBox.setButtonCell(textCell());
        endTimeBox.setButtonCell(textCell());

        // Rooms
        roomBox.getItems().addAll(roomService.getAllRooms());
        roomBox.setCellFactory(lv -> roomCell());
        roomBox.setButtonCell(roomCell());

        // Participants — multi-sélection
        participantList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        participantList.getItems().addAll(userService.getAllUsers());
        participantList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        });
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;

        titleField.setText(reservation.getTitle());
        descriptionField.setText(reservation.getDescription());
        datePicker.setValue(LocalDate.parse(reservation.getDate()));
        startTimeBox.setValue(reservation.getStartTime());
        endTimeBox.setValue(reservation.getEndTime());

        // Select the room
        for (Room room : roomBox.getItems()) {
            if (room.getId().equals(reservation.getRoomId())) {
                roomBox.setValue(room);
                break;
            }
        }

        // Select participants
        List<Long> participantIds = reservationService.getParticipantIds(reservation.getId());
        participantList.getSelectionModel().clearSelection();
        for (int i = 0; i < participantList.getItems().size(); i++) {
            if (participantIds.contains(participantList.getItems().get(i).getId())) {
                participantList.getSelectionModel().select(i);
            }
        }
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("Le titre est obligatoire");
            return;
        }
        if (datePicker.getValue() == null) {
            errorLabel.setText("La date est obligatoire");
            return;
        }
        if (startTimeBox.getValue() == null || endTimeBox.getValue() == null) {
            errorLabel.setText("Les horaires sont obligatoires");
            return;
        }
        if (roomBox.getValue() == null) {
            errorLabel.setText("La salle est obligatoire");
            return;
        }

        reservation.setTitle(title);
        reservation.setDescription(descriptionField.getText());
        reservation.setDate(datePicker.getValue().toString());
        reservation.setStartTime(startTimeBox.getValue());
        reservation.setEndTime(endTimeBox.getValue());
        reservation.setRoomId(roomBox.getValue().getId());

        try {
            reservationService.updateReservation(reservation);

            // Sync participants: remove old, add new
            List<Long> oldIds = reservationService.getParticipantIds(reservation.getId());
            List<User> selectedUsers = new ArrayList<>(participantList.getSelectionModel().getSelectedItems());
            List<Long> newIds = selectedUsers.stream().map(User::getId).toList();

            for (Long oldId : oldIds) {
                if (!newIds.contains(oldId)) {
                    reservationService.removeParticipant(reservation.getId(), oldId);
                }
            }
            for (Long newId : newIds) {
                if (!oldIds.contains(newId)) {
                    reservationService.addParticipant(reservation.getId(), newId);
                }
            }

            ((Stage) titleField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private ListCell<String> textCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: #1e293b;");
            }
        };
    }

    private ListCell<Room> roomCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty || room == null ? null : room.getName() + " (" + room.getCapacity() + " places)");
            }
        };
    }
}
