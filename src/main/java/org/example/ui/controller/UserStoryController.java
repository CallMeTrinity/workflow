package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.model.UserStory;
import org.example.model.enums.Role;
import org.example.service.UserStoryService;
import org.example.ui.util.Modals;

public class UserStoryController {

    @FXML private TableView<UserStory> userStoryTable;
    @FXML private TableColumn<UserStory, String> titleColumn;
    @FXML private TableColumn<UserStory, String> priorityColumn;
    @FXML private TableColumn<UserStory, Void> actionsColumn;
    @FXML private Button addButton;

    private final UserStoryService userStoryService = new UserStoryService();
    private Project project;

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
        CreateUserStoryController controller = Modals.open(userStoryTable,
                "/fxml/createUserStory.fxml", 500, 480, this::loadUserStories);
        controller.setProjectId(project.getId());
    }

    private void handleEdit(UserStory userStory) {
        CreateUserStoryController controller = Modals.open(userStoryTable,
                "/fxml/createUserStory.fxml", 500, 480, this::loadUserStories);
        controller.setProjectId(project.getId());
        controller.setUserStory(userStory);
    }

    private void handleDelete(UserStory userStory) {
        Modals.confirmDelete(userStoryTable,
                "Supprimer la user story \"" + userStory.getTitle() + "\" ?\n"
                        + "Les tâches associées ne seront pas supprimées.",
                () -> {
                    userStoryService.deleteUserStory(userStory.getId());
                    loadUserStories();
                });
    }
}
