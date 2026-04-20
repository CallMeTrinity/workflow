package org.example.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReservationController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> titleColumn;
    @FXML private TableColumn<Reservation, String> dateColumn;
    @FXML private TableColumn<Reservation, String> startColumn;
    @FXML private TableColumn<Reservation, String> endColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;
    @FXML private TableColumn<Reservation, String> organizerColumn;
    @FXML private TableColumn<Reservation, String> participantsColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();
    private final UserService userService = new UserService();

    private Map<Long, Room> roomById;
    private Map<Long, User> userById;
    private ContextMenu activeMenu;

    @FXML
    public void initialize() {
        List<Room> allRooms = roomService.getAllRooms();
        roomById = allRooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));

        List<User> allUsers = userService.getAllUsers();
        userById = allUsers.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        titleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitle()));
        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate()));
        startColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStartTime()));
        endColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEndTime()));
        roomColumn.setCellValueFactory(data -> {
            Room room = roomById.get(data.getValue().getRoomId());
            return new SimpleStringProperty(room != null ? room.getName() : "Inconnue");
        });
        organizerColumn.setCellValueFactory(data -> {
            User organizer = userById.get(data.getValue().getOrganizerId());
            return new SimpleStringProperty(organizer != null
                    ? organizer.getFirstName() + " " + organizer.getLastName() : "—");
        });
        participantsColumn.setCellValueFactory(data -> {
            List<Long> ids = reservationService.getParticipantIds(data.getValue().getId());
            String names = ids.stream()
                    .map(id -> {
                        User u = userById.get(id);
                        return u != null ? u.getFirstName().charAt(0) + ". " + u.getLastName() : "?";
                    })
                    .collect(Collectors.joining(", "));
            if (names.isEmpty()) names = "—";
            return new SimpleStringProperty(names);
        });

        setupActionsColumn();
        setupRowInteractions();
        refreshReservations();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("\u22EE");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; "
                        + "-fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 0 6 0 6;");
                btn.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    showMenu(r, btn, Side.BOTTOM);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupRowInteractions() {
        reservationTable.setRowFactory(tv -> {
            TableRow<Reservation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) return;
                if (event.getButton() == MouseButton.SECONDARY) {
                    showMenu(row.getItem(), row, event.getScreenX(), event.getScreenY());
                } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    openEditReservation(row.getItem());
                }
            });
            return row;
        });
    }

    private ContextMenu buildMenu(Reservation reservation) {
        MenuItem edit = new MenuItem("Voir / Modifier");
        edit.setOnAction(e -> openEditReservation(reservation));

        MenuItem cancel = new MenuItem("Annuler");
        cancel.setStyle("-fx-text-fill: #dc2626;");
        cancel.setOnAction(e -> cancelReservation(reservation));

        return new ContextMenu(edit, new SeparatorMenuItem(), cancel);
    }

    private void showMenu(Reservation reservation, javafx.scene.Node anchor, Side side) {
        closeActiveMenu();
        activeMenu = buildMenu(reservation);
        activeMenu.show(anchor, side, 0, 0);
    }

    private void showMenu(Reservation reservation, javafx.scene.Node anchor, double screenX, double screenY) {
        closeActiveMenu();
        activeMenu = buildMenu(reservation);
        activeMenu.show(anchor, screenX, screenY);
    }

    private void closeActiveMenu() {
        if (activeMenu != null) {
            activeMenu.hide();
            activeMenu = null;
        }
    }

    private void openEditReservation(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editReservation.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Détail réservation");
            stage.setScene(new Scene(loader.load(), 540, 620));
            stage.setMinWidth(480);
            stage.setMinHeight(450);

            EditReservationController controller = loader.getController();
            controller.setReservation(reservation);

            stage.showAndWait();
            refreshReservations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelReservation(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler la réservation « " + reservation.getTitle() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.cancelReservation(reservation.getId());
                    refreshReservations();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                }
            }
        });
    }

    private void refreshReservations() {
        reservationTable.getItems().clear();
        reservationTable.getItems().addAll(reservationService.getAllReservations());
    }

    @FXML
    private void openCreateReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createReservation.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nouvelle réservation");
            stage.setScene(new Scene(loader.load(), 500, 550));
            stage.setMinWidth(460);
            stage.setMinHeight(400);
            stage.showAndWait();
            refreshReservations();
        } catch (Exception e) {
            e.printStackTrace();
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
