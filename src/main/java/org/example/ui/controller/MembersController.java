package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ProjectRepository;
import org.example.repository.UserRepository;
import org.example.service.ProjectService;

import java.util.List;

public class MembersController {

    @FXML private TableView<User>           membersTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> mailColumn;
    @FXML private TableColumn<User, Void>   roleColumn;
    @FXML private TableColumn<User, Void>   actionColumn;
    @FXML private ListView<User>            availableList;
    @FXML private VBox                      addPanel;
    @FXML private Label                     errorLabel;

    private final ProjectService    projectService    = new ProjectService();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final UserRepository    userRepository    = new UserRepository();

    private Project project;
    private boolean canEdit;

    public void setProject(Project project) {
        this.project = project;
        Role role = SessionManager.getCurrentUser().getRole();
        canEdit = role == Role.ADMIN
               || project.getProjectLeaderId().equals(SessionManager.getCurrentUser().getId());

        addPanel.setVisible(canEdit);
        addPanel.setManaged(canEdit);

        setupColumns();
        setupAvailableList();
        load();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFirstName() + " " + data.getValue().getLastName()));

        mailColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getMail()));

        roleColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                User u = (User) getTableRow().getItem();
                Label badge = new Label(roleFr(u.getRole()));
                badge.getStyleClass().addAll("role-badge", "role-badge-" + u.getRole().name().toLowerCase());
                HBox box = new HBox(badge);
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Retirer");
            {
                removeBtn.getStyleClass().add("btn-back");
                removeBtn.setOnAction(e -> handleRemove(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || !canEdit ? null : removeBtn);
            }
        });
    }

    private void setupAvailableList() {
        availableList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null
                        : u.getFirstName() + " " + u.getLastName() + " (" + u.getMail() + ")");
            }
        });
    }

    private void load() {
        List<User> members = projectRepository.findMembers(project.getId());
        membersTable.getItems().setAll(members);

        List<Long> memberIds = members.stream().map(User::getId).toList();
        List<User> available = userRepository.findAll().stream()
                .filter(u -> !memberIds.contains(u.getId()))
                .toList();
        availableList.getItems().setAll(available);
        availableList.getSelectionModel().clearSelection();
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        List<User> selected = availableList.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            errorLabel.setText("Sélectionnez au moins un utilisateur.");
            return;
        }
        try {
            for (User u : List.copyOf(selected)) {
                projectService.addMember(project.getId(), u.getId());
            }
            load();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private String roleFr(Role role) {
        return switch (role) {
            case ADMIN          -> "Admin";
            case PROJECT_LEADER -> "Chef de projet";
            case MEMBER         -> "Membre";
        };
    }

    private void handleRemove(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Retirer " + user.getFirstName() + " " + user.getLastName() + " du projet ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    projectService.removeMember(project.getId(), user.getId());
                    load();
                } catch (Exception e) {
                    errorLabel.setText(e.getMessage());
                }
            }
        });
    }
}
