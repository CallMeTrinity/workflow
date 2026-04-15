package org.example.service;

import org.example.model.Notification;
import org.example.repository.NotificationRepository;

import java.util.List;

public class NotificationService {

    private final NotificationRepository repository = new NotificationRepository();

    public void createNotification(Long userId, String message) {
        createNotification(userId, message, null);
    }

    public void createNotification(Long userId, String message, Long projectId) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        notif.setProjectId(projectId);
        repository.save(notif);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return repository.findByUser(userId);
    }

    public int getUnreadCount(Long userId) {
        return repository.countUnread(userId);
    }

    public void markAllAsRead(Long userId) {
        repository.markAllAsRead(userId);
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
