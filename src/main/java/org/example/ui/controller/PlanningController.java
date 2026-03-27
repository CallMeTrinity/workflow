package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Room;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.service.UserService;

import java.util.List;

public class PlanningController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> durationBox;
    @FXML private ComboBox<Room> roomBox;
    @FXML private ListView<User> participantList;
    @FXML private ListView<String> slotList;
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();
    private final UserService userService = new UserService();

    // Stocke les slots bruts pour la confirmation
    private List<int[]> availableSlots = List.of();

    @FXML
    public void initialize() {
        // Durées disponibles : 30, 60, 90, 120 minutes
        durationBox.getItems().addAll(30, 60, 90, 120);
        durationBox.setValue(60);

        // Salles
        roomBox.getItems().add(null); // option "sans salle"
        roomBox.getItems().addAll(roomService.getAllRooms());
        roomBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty ? null : room == null ? "Sans salle" : room.getName());
            }
        });
        roomBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty ? null : room == null ? "Sans salle" : room.getName());
            }
        });

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

    @FXML
    private void searchSlots() {
        errorLabel.setText("");
        successLabel.setText("");
        slotList.getItems().clear();

        if (datePicker.getValue() == null) {
            errorLabel.setText("Sélectionne une date");
            return;
        }

        List<User> selectedUsers = participantList.getSelectionModel().getSelectedItems();
        if (selectedUsers.isEmpty()) {
            errorLabel.setText("Sélectionne au moins un participant");
            return;
        }

        String date = datePicker.getValue().toString();
        int duration = durationBox.getValue();
        Room selectedRoom = roomBox.getValue();
        Long roomId = selectedRoom != null ? selectedRoom.getId() : null;

        List<Long> userIds = selectedUsers.stream().map(User::getId).toList();

        availableSlots = reservationService.findAvailableSlots(userIds, date, duration, roomId);

        if (availableSlots.isEmpty()) {
            errorLabel.setText("Aucun créneau disponible pour ces paramètres");
            return;
        }

        for (int[] slot : availableSlots) {
            int start = slot[0];
            while (start + duration <= slot[1]) {
                slotList.getItems().add(
                        toTimeString(start) + " - " + toTimeString(start + duration)
                );
                start += 30; // pas de 30 minutes
            }
        }
    }

    @FXML
    private void confirmReservation() {
        errorLabel.setText("");
        successLabel.setText("");

        int selectedIndex = slotList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            errorLabel.setText("Sélectionne un créneau");
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("Le titre est obligatoire");
            return;
        }

        if (datePicker.getValue() == null) {
            errorLabel.setText("Sélectionne une date");
            return;
        }

        String selected = slotList.getSelectionModel().getSelectedItem();
        String[] parts = selected.split(" - ");
        String startTime = parts[0];
        String endTime = parts[1];
        String date = datePicker.getValue().toString();
        Room selectedRoom = roomBox.getValue();
        Long roomId = selectedRoom != null ? selectedRoom.getId() : null;
        Long organizerId = SessionManager.getCurrentUser().getId();

        try {
            var reservation = reservationService.createReservation(
                    title,
                    descriptionField.getText(),
                    date,
                    startTime,
                    endTime,
                    roomId,
                    null,
                    organizerId
            );

            // Ajouter les participants
            List<User> selectedUsers = participantList.getSelectionModel().getSelectedItems();
            for (User user : selectedUsers) {
                reservationService.addParticipant(reservation.getId(), user.getId());
            }

            successLabel.setText("Réservation créée : " + startTime + " - " + endTime);
            slotList.getItems().clear();
            availableSlots = List.of();
            titleField.clear();
            descriptionField.clear();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private String toTimeString(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
