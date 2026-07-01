package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.config.SessionManager;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.ui.util.Modals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateReservationController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> startTimeBox;
    @FXML private ComboBox<String> endTimeBox;
    @FXML private ComboBox<Room> roomBox;
    @FXML private Label errorLabel;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();

    private Reservation reservationToEdit = null;

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

        List<String> slots = generateTimeSlots();
        startTimeBox.getItems().addAll(slots);
        endTimeBox.getItems().addAll(slots);

        // Quand on choisit une heure de début,
        // l'heure de fin minimum = début + 30min
        startTimeBox.setOnAction(e -> {
            String selected = startTimeBox.getValue();
            if (selected == null) return;
            int startMinutes = toMinutes(selected);
            endTimeBox.getItems().clear();
            endTimeBox.getItems().addAll(
                    slots.stream()
                            .filter(s -> toMinutes(s) > startMinutes)
                            .toList()
            );
        });
    }

    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        for (int minutes = 0; minutes < 24 * 60; minutes += 30) {
            slots.add(String.format("%02d:%02d", minutes / 60, minutes % 60));
        }
        return slots;
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
    /** Pre-fills date and time for a new reservation (e.g. from calendar slot click). */
    public void prefill(LocalDate date, String startTime, String endTime) {
        datePicker.setValue(date);
        startTimeBox.setValue(startTime);
        // The startTimeBox action listener rebuilds endTimeBox items; set value after
        int startMins = toMinutes(startTime);
        List<String> allSlots = generateTimeSlots();
        endTimeBox.getItems().setAll(allSlots.stream().filter(s -> toMinutes(s) > startMins).toList());
        endTimeBox.setValue(endTime);
    }

    /** Populates all fields for editing an existing reservation. */
    public void setReservation(Reservation r) {
        this.reservationToEdit = r;
        titleField.setText(r.getTitle());
        if (r.getDescription() != null) descriptionField.setText(r.getDescription());
        prefill(LocalDate.parse(r.getDate()), r.getStartTime(), r.getEndTime());
        roomBox.getItems().stream()
                .filter(room -> room != null && room.getId().equals(r.getRoomId()))
                .findFirst()
                .ifPresent(roomBox::setValue);
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        if (datePicker.getValue() == null) {
            errorLabel.setText("La date est obligatoire");
            return;
        }
        String date = datePicker.getValue().toString();
        String startTime = startTimeBox.getValue();
        String endTime = endTimeBox.getValue();
        Room selectedRoom = roomBox.getValue();

        if (title.isEmpty() || datePicker.getValue() == null
                || startTime == null || endTime == null || selectedRoom == null) {
            errorLabel.setText("Tous les champs obligatoires doivent être remplis");
            return;
        }

        try {
            if (reservationToEdit != null) {
                reservationToEdit.setTitle(title);
                reservationToEdit.setDescription(descriptionField.getText());
                reservationToEdit.setDate(date);
                reservationToEdit.setStartTime(startTime);
                reservationToEdit.setEndTime(endTime);
                reservationToEdit.setRoomId(selectedRoom.getId());
                reservationService.updateReservation(reservationToEdit);
            } else {
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
            }
            Modals.close(titleField);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
