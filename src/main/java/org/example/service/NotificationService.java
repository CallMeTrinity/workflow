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

    public void markAsRead(Long id) {
        Notification notif = repository.findById(id);
        if (notif != null) {
            notif.setIsRead(true);
            repository.update(notif);
        }
    }

    public void markAsUnread(Long id) {
        Notification notif = repository.findById(id);
        if (notif != null) {
            notif.setIsRead(false);
            repository.update(notif);
        }
    }

    public void deleteNotification(Long id) {
        repository.delete(id);
    }

    public int countUnread(Long id) {
        try {
            return repository.countUnread(id);
        } catch (Exception e) {
            return 0;
        }
    }

}