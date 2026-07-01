package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.config.SessionManager;
import org.example.model.Room;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.service.UserService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlanningController {

    private static final Logger LOGGER = Logger.getLogger(PlanningController.class.getName());

    @FXML private CheckBox asapCheckBox;
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

    /** Créneaux affichés : [startMinutes, endMinutes, roomId, dayOffset] */
    private final List<int[]> displayedSlots = new ArrayList<>();

    /** Dates correspondant aux créneaux affichés (pour mode "au plus tôt") */
    private final List<LocalDate> displayedDates = new ArrayList<>();

    private Map<Long, Room> roomById;

    /** Nombre max de jours ouvrés à scanner en mode "au plus tôt" */
    private static final int ASAP_MAX_DAYS = 20;

    @FXML
    public void initialize() {
        durationBox.getItems().addAll(30, 60, 90, 120);
        durationBox.setValue(60);

        List<Room> allRooms = roomService.getAllRooms();
        roomById = allRooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));

        // Salles
        roomBox.getItems().add(null);
        roomBox.getItems().addAll(allRooms);
        roomBox.setCellFactory(lv -> roomCell());
        roomBox.setButtonCell(roomCell());

        // Participants
        participantList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        participantList.getItems().addAll(userService.getAllUsers());
        participantList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        });

        // Bloquer les weekends dans le DatePicker
        datePicker.setDayCellFactory(disableWeekends());
    }

    /** Désactive samedi et dimanche dans le DatePicker. */
    private Callback<DatePicker, DateCell> disableWeekends() {
        return dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                        || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #e2e8f0; -fx-opacity: 0.5;");
                }
            }
        };
    }

    @FXML
    private void toggleAsap() {
        datePicker.setDisable(asapCheckBox.isSelected());
        if (asapCheckBox.isSelected()) {
            datePicker.setValue(null);
        }
    }

    @FXML
    private void searchSlots() {
        errorLabel.setText("");
        successLabel.setText("");
        slotList.getItems().clear();
        displayedSlots.clear();
        displayedDates.clear();

        List<User> selectedUsers = participantList.getSelectionModel().getSelectedItems();
        if (selectedUsers.isEmpty()) {
            errorLabel.setText("Sélectionne au moins un participant");
            return;
        }

        boolean asap = asapCheckBox.isSelected();

        if (!asap && datePicker.getValue() == null) {
            errorLabel.setText("Sélectionne une date ou coche « Au plus tôt »");
            return;
        }

        int duration = durationBox.getValue();
        Room selectedRoom = roomBox.getValue();
        List<Long> userIds = selectedUsers.stream().map(User::getId).toList();

        if (selectedRoom != null && selectedUsers.size() > selectedRoom.getCapacity()) {
            errorLabel.setText("La salle « " + selectedRoom.getName()
                    + " » ne peut accueillir que " + selectedRoom.getCapacity()
                    + " personnes (" + selectedUsers.size() + " sélectionnées)");
            return;
        }

        if (asap) {
            searchAsap(userIds, duration, selectedRoom);
        } else {
            searchForDate(datePicker.getValue(), userIds, duration, selectedRoom);
        }

        if (displayedSlots.isEmpty()) {
            errorLabel.setText("Aucun créneau disponible pour ces paramètres");
        }
    }

    /** Recherche sur une date précise. */
    private void searchForDate(LocalDate date, List<Long> userIds, int duration, Room selectedRoom) {
        String dateStr = date.toString();

        if (selectedRoom != null) {
            addFixedRoomSlots(date, dateStr, userIds, duration, selectedRoom, false);
        } else {
            addAutoRoomSlots(date, dateStr, userIds, duration, false);
        }
    }

    /** Recherche "au plus tôt" : scanne les prochains jours ouvrés et s'arrête dès qu'un créneau est trouvé. */
    private void searchAsap(List<Long> userIds, int duration, Room selectedRoom) {
        LocalDate day = LocalDate.now();
        // Si aujourd'hui est un weekend, avancer au lundi
        day = skipToWeekday(day);

        int scanned = 0;
        while (scanned < ASAP_MAX_DAYS) {
            String dateStr = day.toString();
            int before = displayedSlots.size();

            if (selectedRoom != null) {
                addFixedRoomSlots(day, dateStr, userIds, duration, selectedRoom, true);
            } else {
                addAutoRoomSlots(day, dateStr, userIds, duration, true);
            }

            // Dès qu'on trouve des créneaux sur un jour, on s'arrête
            if (displayedSlots.size() > before) {
                return;
            }

            day = skipToWeekday(day.plusDays(1));
            scanned++;
        }
    }

    private void addFixedRoomSlots(LocalDate date, String dateStr, List<Long> userIds,
                                    int duration, Room room, boolean showDate) {
        List<int[]> availableSlots = reservationService.findAvailableSlots(
                userIds, dateStr, duration, room.getId());

        String datePrefix = showDate ? formatDate(date) + "  " : "";

        for (int[] slot : availableSlots) {
            int start = slot[0];
            while (start + duration <= slot[1]) {
                displayedSlots.add(new int[]{start, start + duration, room.getId().intValue()});
                displayedDates.add(date);
                slotList.getItems().add(
                        datePrefix + toTimeString(start) + " - " + toTimeString(start + duration)
                                + "  [" + room.getName() + "]"
                );
                start += 30;
            }
        }
    }

    private void addAutoRoomSlots(LocalDate date, String dateStr, List<Long> userIds,
                                   int duration, boolean showDate) {
        List<int[]> autoSlots = reservationService.findAvailableSlotsWithAutoRoom(
                userIds, dateStr, duration, new ArrayList<>(roomById.values()));

        String datePrefix = showDate ? formatDate(date) + "  " : "";

        for (int[] slot : autoSlots) {
            int slotStart = slot[0];
            int slotEnd = slot[1];
            long roomId = slot[2];
            Room room = roomById.get(roomId);

            int start = slotStart;
            while (start + duration <= slotEnd) {
                displayedSlots.add(new int[]{start, start + duration, (int) roomId});
                displayedDates.add(date);
                slotList.getItems().add(
                        datePrefix + toTimeString(start) + " - " + toTimeString(start + duration)
                                + "  [" + room.getName() + " (" + room.getCapacity() + " places)]"
                );
                start += 30;
            }
        }

        deduplicateSlots();
    }

    private void deduplicateSlots() {
        List<int[]> uniqueSlots = new ArrayList<>();
        List<String> uniqueLabels = new ArrayList<>();
        List<LocalDate> uniqueDates = new ArrayList<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();

        for (int i = 0; i < displayedSlots.size(); i++) {
            int[] slot = displayedSlots.get(i);
            LocalDate date = displayedDates.get(i);
            String key = date + "-" + slot[0] + "-" + slot[1];
            if (seen.add(key)) {
                uniqueSlots.add(slot);
                uniqueLabels.add(slotList.getItems().get(i));
                uniqueDates.add(date);
            }
        }

        displayedSlots.clear();
        displayedSlots.addAll(uniqueSlots);
        displayedDates.clear();
        displayedDates.addAll(uniqueDates);
        slotList.getItems().clear();
        slotList.getItems().addAll(uniqueLabels);
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

        int[] chosen = displayedSlots.get(selectedIndex);
        LocalDate date = displayedDates.get(selectedIndex);
        String startTime = toTimeString(chosen[0]);
        String endTime = toTimeString(chosen[1]);
        long roomId = chosen[2];
        Long organizerId = SessionManager.getCurrentUser().getId();

        List<User> selectedUsers = participantList.getSelectionModel().getSelectedItems();
        List<Long> participantIds = selectedUsers.stream().map(User::getId).toList();
        List<Long> conflictingIds = reservationService.findConflictingUserIds(
                participantIds, date.toString(), startTime, endTime);
        if (!conflictingIds.isEmpty()) {
            String names = conflictingIds.stream()
                    .map(id -> selectedUsers.stream()
                            .filter(u -> u.getId().equals(id))
                            .findFirst()
                            .map(User::getFullName)
                            .orElse("?"))
                    .collect(Collectors.joining(", "));
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ces participants ont déjà une réunion sur ce créneau :\n" + names
                            + "\n\nContinuer quand même ?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> response = confirm.showAndWait();
            if (response.isEmpty() || response.get() != ButtonType.YES) return;
        }

        try {
            var reservation = reservationService.createReservation(
                    title,
                    descriptionField.getText(),
                    date.toString(),
                    startTime,
                    endTime,
                    roomId,
                    null,
                    organizerId
            );

            for (User user : selectedUsers) {
                reservationService.addParticipant(reservation.getId(), user.getId());
            }

            Room room = roomById.get(roomId);
            String roomName = room != null ? room.getName() : "?";
            successLabel.setText("Réservation créée : " + formatDate(date)
                    + " " + startTime + " - " + endTime + " — " + roomName);
            slotList.getItems().clear();
            displayedSlots.clear();
            displayedDates.clear();
            titleField.clear();
            descriptionField.clear();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private String toTimeString(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private String formatDate(LocalDate date) {
        String[] jours = {"lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim."};
        return jours[date.getDayOfWeek().getValue() - 1] + " " + date;
    }

    private LocalDate skipToWeekday(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private ListCell<Room> roomCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty ? null : room == null ? "Automatique" : room.getName());
            }
        };
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }
}
