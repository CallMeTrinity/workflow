package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.model.UserStory;
import org.example.model.enums.Role;
import org.example.service.UserStoryService;

public class UserStoryController {

    @FXML private TableView<UserStory> userStoryTable;
    @FXML private TableColumn<UserStory, String> titleColumn;
    @FXML private TableColumn<UserStory, String> priorityColumn;
    @FXML private TableColumn<UserStory, Void> actionsColumn;
    @FXML private Button addButton;

    private final UserStoryService userStoryService = new UserStoryService();
    private Project project;
    private Stage createUserStoryStage;

    public void setProject(Project project) {
        this.project = project;
        loadUserStories();

        Role role = SessionManager.getCurrentUser().getRole();
        addButton.setVisible(role == Role.ADMIN || role == Role.PROJECT_LEADER);
    }

    @FXML
    public void initialize() {
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final MenuButton menuButton = new MenuButton("\u22ee");

            {
                MenuItem editItem = new MenuItem("Modifier");
                editItem.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                MenuItem deleteItem = new MenuItem("Supprimer");
                deleteItem.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                menuButton.getItems().addAll(editItem, deleteItem);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Role role = SessionManager.getCurrentUser().getRole();
                    if (role == Role.ADMIN || role == Role.PROJECT_LEADER) {
                        setGraphic(menuButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadUserStories() {
        userStoryTable.getItems().clear();
        userStoryTable.getItems().addAll(userStoryService.getUserStoriesByProject(project.getId()));
    }

    @FXML
    private void handleAdd() {
        if (createUserStoryStage != null && createUserStoryStage.isShowing()) createUserStoryStage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createUserStory.fxml"));
            createUserStoryStage = new Stage();
            createUserStoryStage.setScene(new Scene(loader.load(), 480, 420));
            createUserStoryStage.setMinWidth(400);
            createUserStoryStage.setMinHeight(350);
            createUserStoryStage.sizeToScene();
            createUserStoryStage.setTitle("Créer une User Story");
            createUserStoryStage.focusedProperty().addListener((obs, wasFocused, focused) -> {
                if (!focused) createUserStoryStage.close();
            });
            createUserStoryStage.setOnHidden(e -> loadUserStories());

            CreateUserStoryController controller = loader.getController();
            controller.setProjectId(project.getId());

            createUserStoryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(UserStory userStory) {
        if (createUserStoryStage != null && createUserStoryStage.isShowing()) createUserStoryStage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createUserStory.fxml"));
            createUserStoryStage = new Stage();
            createUserStoryStage.setScene(new Scene(loader.load(), 480, 420));
            createUserStoryStage.setMinWidth(400);
            createUserStoryStage.setMinHeight(350);
            createUserStoryStage.sizeToScene();
            createUserStoryStage.setTitle("Modifier la User Story");
            createUserStoryStage.focusedProperty().addListener((obs, wasFocused, focused) -> {
                if (!focused) createUserStoryStage.close();
            });
            createUserStoryStage.setOnHidden(e -> loadUserStories());

            CreateUserStoryController controller = loader.getController();
            controller.setProjectId(project.getId());
            controller.setUserStory(userStory);

            createUserStoryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(UserStory userStory) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la user story \"" + userStory.getTitle() + "\" ?\n"
                        + "Les tâches associées ne seront pas supprimées.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                userStoryService.deleteUserStory(userStory.getId());
                loadUserStories();
            }
        });
    }
}
