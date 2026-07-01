package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.model.Notification;
import org.example.model.Project;
import org.example.model.User;
import org.example.service.NotificationService;
import org.example.service.ProjectService;
import org.example.service.UserService;
import org.example.ui.util.AvatarUtil;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger LOGGER = Logger.getLogger(ProfileController.class.getName());

    @FXML private Label avatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private TextField pseudoField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel;
    @FXML private Label unreadCountLabel;
    @FXML private ListView<Notification> notificationList;

    private UserService userService;
    private NotificationService notificationService;
    private final ProjectService projectService = new ProjectService();
    private User currentUser;

    public void init(User user, UserService userService, NotificationService notificationService) {
        this.currentUser = user;
        this.userService = userService;
        this.notificationService = notificationService;

        emailField.setText(user.getMail());
        pseudoField.setText(user.getUsername());

        String roleName = switch (user.getRole()) {
            case ADMIN -> "Administrateur";
            case PROJECT_LEADER -> "Chef de projet";
            case MEMBER -> "Membre";
        };
        roleLabel.setText(roleName);
        roleLabel.getStyleClass().add("role-badge-" + user.getRole().name().toLowerCase());

        updateAvatar(user.getUsername());
        loadNotifications();

        pseudoField.setOnAction(e -> {
            String newPseudo = pseudoField.getText().trim();
            if (newPseudo.isEmpty()) return;

            userService.updateUsername(currentUser.getId(), newPseudo);
            currentUser.setUsername(newPseudo);
            updateAvatar(newPseudo);
        });

        notificationList.setPlaceholder(new Label("Aucune notification"));
    }

    private void updateAvatar(String pseudo) {
        if (pseudo == null || pseudo.isEmpty()) {
            avatarLabel.setText("?");
            return;
        }
        avatarLabel.setText(pseudo.substring(0, 1).toUpperCase());
        avatarCircle.setStyle("-fx-fill: " + AvatarUtil.generateColor(pseudo));
    }

    private void loadNotifications() {
        List<Notification> list = notificationService.getUserNotifications(currentUser.getId());
        notificationList.getItems().setAll(list);

        long unread = list.stream().filter(n -> !n.isRead()).count();
        unreadCountLabel.setText(unread > 0 ? unread + " non lue" + (unread > 1 ? "s" : "") : "");

        notificationList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification notif, boolean empty) {
                super.updateItem(notif, empty);
                if (empty || notif == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                    return;
                }

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 8 12;");

                // Unread indicator
                if (!notif.isRead()) {
                    Region dot = new Region();
                    dot.setMinSize(8, 8);
                    dot.setMaxSize(8, 8);
                    dot.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 4;");
                    row.getChildren().add(dot);
                }

                Label msg = new Label(notif.getMessage());
                msg.setWrapText(true);
                msg.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(msg, Priority.ALWAYS);
                if (!notif.isRead()) {
                    msg.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
                } else {
                    msg.setStyle("-fx-text-fill: #64748b;");
                }
                row.getChildren().add(msg);

                Button toggleBtn = new Button(notif.isRead() ? "Non lu" : "Lu");
                toggleBtn.getStyleClass().add("btn-back");
                toggleBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 8;");
                toggleBtn.setOnAction(e -> {
                    notificationService.toggleRead(notif.getId());
                    loadNotifications();
                });

                Button deleteBtn = new Button("×");
                deleteBtn.getStyleClass().add("btn-back");
                deleteBtn.setStyle("-fx-font-size: 13px; -fx-padding: 2 6; -fx-text-fill: #dc2626;");
                deleteBtn.setOnAction(e -> {
                    notificationService.deleteNotification(notif.getId());
                    loadNotifications();
                });

                row.getChildren().addAll(toggleBtn, deleteBtn);

                if (notif.getProjectId() != null) {
                    Button kanbanBtn = new Button("Kanban →");
                    kanbanBtn.getStyleClass().add("btn-back");
                    kanbanBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-text-fill: #3b82f6;");
                    kanbanBtn.setOnAction(e -> openKanban(notif.getProjectId()));
                    row.getChildren().add(kanbanBtn);
                }

                setGraphic(row);
                setText(null);
            }
        });
    }

    @FXML
    private void refreshNotifications() {
        loadNotifications();
    }

    @FXML
    private void markAllAsRead() {
        notificationService.markAllAsRead(currentUser.getId());
        loadNotifications();
    }

    private void openKanban(Long projectId) {
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kanban.fxml"));
            Stage profileStage = (Stage) notificationList.getScene().getWindow();
            Stage mainStage = (Stage) profileStage.getOwner();

            if (mainStage == null) {
                // Fallback: use any existing stage
                mainStage = profileStage;
            }

            mainStage.getScene().setRoot(loader.load());
            mainStage.setTitle("Kanban - " + project.getName());
            KanbanController controller = loader.getController();
            controller.setProject(project);

            if (profileStage != mainStage) {
                profileStage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }
}
