package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
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
    @FXML private TableColumn<Room, Void> actionsColumn;
    @FXML private HBox adminButtons;

    private final RoomService roomService = new RoomService();

    private ContextMenu activeMenu;

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

        setupActionsColumn();
        setupRowContextMenu();
        refreshRooms();
    }

    private void refreshRooms() {
        roomTable.getItems().clear();
        List<Room> rooms = roomService.getAllRooms();
        roomTable.getItems().addAll(rooms);
    }

    /* ------------------------------------------------------------------ */
    /*  Colonne "⋮"                                                       */
    /* ------------------------------------------------------------------ */

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⋮");

            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; "
                        + "-fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 0 6 0 6;");
                btn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    showRoomMenu(room, btn, Side.BOTTOM);
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

    /* ------------------------------------------------------------------ */
    /*  Clic droit sur une ligne                                          */
    /* ------------------------------------------------------------------ */

    private void setupRowContextMenu() {
        roomTable.setRowFactory(tv -> {
            TableRow<Room> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    showRoomMenu(row.getItem(), row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Construction + affichage du menu contextuel                        */
    /* ------------------------------------------------------------------ */

    private ContextMenu buildRoomMenu(Room room) {
        MenuItem edit = new MenuItem("Modifier");
        edit.setOnAction(e -> openEditRoom(room));

        MenuItem delete = new MenuItem("Supprimer");
        delete.setStyle("-fx-text-fill: #dc2626;");
        delete.setOnAction(e -> deleteRoom(room));

        return new ContextMenu(edit, new SeparatorMenuItem(), delete);
    }

    /** Affiche à côté d'un nœud (bouton ⋮). */
    private void showRoomMenu(Room room, javafx.scene.Node anchor, Side side) {
        closeActiveMenu();
        activeMenu = buildRoomMenu(room);
        activeMenu.show(anchor, side, 0, 0);
    }

    /** Affiche à une position écran (clic droit). */
    private void showRoomMenu(Room room, javafx.scene.Node anchor, double screenX, double screenY) {
        closeActiveMenu();
        activeMenu = buildRoomMenu(room);
        activeMenu.show(anchor, screenX, screenY);
    }

    private void closeActiveMenu() {
        if (activeMenu != null) {
            activeMenu.hide();
            activeMenu = null;
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Actions projet                                                    */
    /* ------------------------------------------------------------------ */

    private void openEditRoom(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editRoom.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier la salle");
            stage.setScene(new Scene(loader.load(), 460, 320));
            stage.setMinWidth(380);
            stage.setMinHeight(280);

            EditRoomController controller = loader.getController();
            controller.setRoom(room);

            stage.showAndWait();
            refreshRooms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteRoom(Room room) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le projet \"" + room.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                roomService.deleteRoom(room.getId());
                refreshRooms();
            }
        });
    }

    @FXML
    private void openCreateRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createRoom.fxml"));
            javafx.stage.Stage stage = new Stage();
            stage.setTitle("Ajouter une salle");
            stage.setScene(new javafx.scene.Scene(loader.load(), 460, 320));
            stage.setMinWidth(380);
            stage.setMinHeight(280);
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
