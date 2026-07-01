package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.example.config.SessionManager;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.service.UserService;
import org.example.ui.util.Modals;

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
    @FXML private FlowPane participantChips;
    @FXML private ComboBox<User> addParticipantBox;
    @FXML private Label organizerLabel;
    @FXML private Label errorLabel;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();
    private final UserService userService = new UserService();

    private Reservation reservation;
    private final List<User> selectedParticipants = new ArrayList<>();
    private List<User> allUsers;

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

        // Load all users (exclude current user from the add list)
        allUsers = userService.getAllUsers();
        Long myId = SessionManager.getCurrentUser().getId();

        // Add current user as automatic participant
        allUsers.stream()
                .filter(u -> u.getId().equals(myId))
                .findFirst()
                .ifPresent(selectedParticipants::add);

        // ComboBox for adding participants
        addParticipantBox.setCellFactory(lv -> userCell());
        addParticipantBox.setButtonCell(userCell());
        addParticipantBox.setOnAction(e -> {
            User selected = addParticipantBox.getValue();
            if (selected != null && selectedParticipants.stream().noneMatch(u -> u.getId().equals(selected.getId()))) {
                selectedParticipants.add(selected);
                refreshChips();
                refreshAddComboBox();
            }
            addParticipantBox.setValue(null);
        });

        refreshChips();
        refreshAddComboBox();
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;

        titleField.setText(reservation.getTitle());
        descriptionField.setText(reservation.getDescription());
        datePicker.setValue(LocalDate.parse(reservation.getDate()));

        // Show organizer name
        User organizer = userService.getUserById(reservation.getOrganizerId());
        if (organizer != null) {
            organizerLabel.setText("Créé par " + organizer.getFirstName() + " " + organizer.getLastName());
        }
        startTimeBox.setValue(reservation.getStartTime());
        endTimeBox.setValue(reservation.getEndTime());

        // Select the room
        for (Room room : roomBox.getItems()) {
            if (room.getId().equals(reservation.getRoomId())) {
                roomBox.setValue(room);
                break;
            }
        }

        // Load existing participants
        List<Long> participantIds = reservationService.getParticipantIds(reservation.getId());
        selectedParticipants.clear();
        Long myId = SessionManager.getCurrentUser().getId();
        // Always include current user
        allUsers.stream().filter(u -> u.getId().equals(myId)).findFirst().ifPresent(selectedParticipants::add);
        for (Long pid : participantIds) {
            if (!pid.equals(myId)) {
                allUsers.stream().filter(u -> u.getId().equals(pid)).findFirst().ifPresent(selectedParticipants::add);
            }
        }
        refreshChips();
        refreshAddComboBox();
    }

    private void refreshChips() {
        participantChips.getChildren().clear();
        Long myId = SessionManager.getCurrentUser().getId();

        for (User user : selectedParticipants) {
            boolean isMe = user.getId().equals(myId);
            HBox chip = new HBox(4);
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.getStyleClass().add("participant-chip");

            Label name = new Label(user.getFullName() + (isMe ? " (vous)" : ""));
            name.getStyleClass().add("participant-chip-name");
            chip.getChildren().add(name);

            if (!isMe) {
                Button remove = new Button("×");
                remove.getStyleClass().add("participant-chip-remove");
                remove.setOnAction(e -> {
                    selectedParticipants.removeIf(u -> u.getId().equals(user.getId()));
                    refreshChips();
                    refreshAddComboBox();
                });
                chip.getChildren().add(remove);
            }

            participantChips.getChildren().add(chip);
        }
    }

    private void refreshAddComboBox() {
        addParticipantBox.getItems().clear();
        List<Long> selectedIds = selectedParticipants.stream().map(User::getId).toList();
        allUsers.stream()
                .filter(u -> !selectedIds.contains(u.getId()))
                .forEach(addParticipantBox.getItems()::add);
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
            List<Long> newIds = selectedParticipants.stream().map(User::getId).toList();

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

            Modals.close(titleField);
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

    private ListCell<User> userCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        };
    }
}
