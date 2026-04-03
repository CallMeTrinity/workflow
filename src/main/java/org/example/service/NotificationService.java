package org.example.service;

import org.example.model.Notification;
import org.example.repository.NotificationRepository;
import java.util.List;

public class NotificationService {

    private final NotificationRepository repository = new NotificationRepository();

    public void sendNotification(String message, Long userId) {
        Notification notif = new Notification(null, message, userId, false);
        repository.save(notif);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return repository.findByUser(userId);
    }

    public void markAsRead(Notification notif) {
        notif.setIsRead(true);
        repository.update(notif);
    }
}