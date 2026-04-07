package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.model.Notification;
import org.example.model.User;
import org.example.service.NotificationService;
import org.example.service.UserService;
import org.example.ui.util.AvatarUtil;

import java.util.List;

public class ProfileController {

    @FXML private Label avatarLabel;
    @FXML private Circle avatarCircle;

    @FXML private TextField pseudoField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;

    @FXML private ListView<Notification> notificationList;

    private UserService userService;
    private NotificationService notificationService;
    private User currentUser;

    public void init(User user, UserService userService, NotificationService notificationService) {
        this.currentUser = user;
        this.userService = userService;
        this.notificationService = notificationService;

        emailField.setText(user.getMail());
        roleField.setText(user.getRole().name());
        pseudoField.setText(user.getUsername());

        updateAvatar(user.getUsername());
        loadNotifications();

        pseudoField.setOnAction(e -> {
            String newPseudo = pseudoField.getText().trim();

            if (newPseudo.isEmpty()) return;

            userService.updateUsername(currentUser.getId(), newPseudo);
            currentUser.setUsername(newPseudo);
            updateAvatar(newPseudo);

            Stage stage = (Stage) pseudoField.getScene().getWindow();
            stage.close();
        });
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

        notificationList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification notif, boolean empty) {
                super.updateItem(notif, empty);

                if (empty || notif == null) {
                    setGraphic(null);
                    return;
                }

                Label label = new Label(notif.getMessage());
                label.setWrapText(true);
                label.setMaxWidth(250);

                if (!notif.isRead()) {
                    label.setStyle("-fx-font-weight: bold;");
                }

                Button toggleBtn = new Button(notif.isRead() ? "Non lu" : "Lu");
                Button deleteBtn = new Button("🗑");

                toggleBtn.setOnAction(e -> {
                    notificationService.toggleRead(notif.getId());
                    loadNotifications();
                });

                deleteBtn.setOnAction(e -> {
                    notificationService.deleteNotification(notif.getId());
                    loadNotifications();
                });

                VBox actions = new VBox(5, toggleBtn, deleteBtn);
                actions.setAlignment(Pos.CENTER);

                HBox content = new HBox(15, label, actions);
                content.setAlignment(Pos.CENTER_LEFT);

                content.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-padding: 12;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #e5e7eb;"
                );

                setGraphic(content);
            }
        });
    }

    @FXML
    private void markAllAsRead() {
        notificationService.markAllAsRead(currentUser.getId());
        loadNotifications();
    }
}


