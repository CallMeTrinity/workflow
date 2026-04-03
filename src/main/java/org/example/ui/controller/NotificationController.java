package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.example.config.SessionManager;
import org.example.model.Notification;
import org.example.service.NotificationService;

import java.util.List;

public class NotificationController {

    @FXML private ListView<Notification> notificationList;

    private final NotificationService service = new NotificationService();

    @FXML
    public void initialize() {
        loadNotifications();
    }

    private void loadNotifications() {
        Long userId = SessionManager.getCurrentUser().getId();

        List<Notification> notifications = service.getUserNotifications(userId);
        notificationList.getItems().setAll(notifications);
    }
}