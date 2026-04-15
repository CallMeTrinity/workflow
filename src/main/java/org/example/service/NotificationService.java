package org.example.service;

import org.example.model.Notification;
import org.example.repository.NotificationRepository;

import java.util.List;

public class NotificationService {

    private final NotificationRepository repository = new NotificationRepository();

    public void createNotification(Long userId, String message) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        repository.save(notif);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return repository.findByUser(userId);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> notifications = repository.findByUser(userId);

        for (Notification notif : notifications) {
            if (!notif.isRead()) {
                notif.setIsRead(true);
                repository.update(notif);
            }
        }
    }

    public void toggleRead(Long id) {
        Notification notif = repository.findById(id);

        if (notif != null) {
            notif.setIsRead(!notif.isRead());
            repository.update(notif);
        }
    }

    public void deleteNotification(Long id) {
        repository.delete(id);
    }
}