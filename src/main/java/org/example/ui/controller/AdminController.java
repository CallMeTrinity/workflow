package org.example.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.service.UserService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controleur du panneau d'administration.
 * Permet la gestion des utilisateurs (CRUD) et l'acces a la gestion des salles.
 * Accessible uniquement aux administrateurs.
 */
public class AdminController {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    private final UserService userService = new UserService();
    private ContextMenu activeMenu;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFullName()));
        emailColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMail()));
        usernameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername() != null
                        ? data.getValue().getUsername() : "—"));
        roleColumn.setCellValueFactory(data -> {
            Role role = data.getValue().getRole();
            String label = switch (role) {
                case ADMIN -> "Administrateur";
                case PROJECT_LEADER -> "Chef de projet";
                case MEMBER -> "Membre";
            };
            return new SimpleStringProperty(label);
        });

        setupActionsColumn();
        setupRowInteractions();
        refreshUsers();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("\u22EE");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; "
                        + "-fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 0 6 0 6;");
                btn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showMenu(user, btn, Side.BOTTOM);
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
        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) return;
                if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    showMenu(row.getItem(), row, event.getScreenX(), event.getScreenY());
                } else if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY
                        && event.getClickCount() == 2) {
                    openEditUser(row.getItem());
                }
            });
            return row;
        });
    }

    private ContextMenu buildMenu(User user) {
        MenuItem edit = new MenuItem("Modifier");
        edit.setOnAction(e -> openEditUser(user));

        ContextMenu menu = new ContextMenu(edit);

        MenuItem delete = new MenuItem("Supprimer");
        delete.setStyle("-fx-text-fill: #dc2626;");
        delete.setOnAction(e -> deleteUser(user));
        menu.getItems().addAll(new SeparatorMenuItem(), delete);

        return menu;
    }

    private void showMenu(User user, javafx.scene.Node anchor, Side side) {
        closeActiveMenu();
        activeMenu = buildMenu(user);
        activeMenu.show(anchor, side, 0, 0);
    }

    private void showMenu(User user, javafx.scene.Node anchor, double screenX, double screenY) {
        closeActiveMenu();
        activeMenu = buildMenu(user);
        activeMenu.show(anchor, screenX, screenY);
    }

    private void closeActiveMenu() {
        if (activeMenu != null) {
            activeMenu.hide();
            activeMenu = null;
        }
    }

    private void openEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createUser.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(loader.load(), 460, 520));
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.sizeToScene();
            stage.setOnHidden(e -> refreshUsers());
            CreateUserController controller = loader.getController();
            controller.setUser(user);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'utilisateur \"" + user.getFullName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userService.deleteUser(user.getId());
                    refreshUsers();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
                }
            }
        });
    }

    @FXML
    private void openCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createUser.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nouvel utilisateur");
            stage.setScene(new Scene(loader.load(), 460, 520));
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.sizeToScene();
            stage.setOnHidden(e -> refreshUsers());
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    @FXML
    private void openRooms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Salles");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    private void refreshUsers() {
        userTable.getItems().clear();
        userTable.getItems().addAll(userService.getAllUsers());
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }
}
