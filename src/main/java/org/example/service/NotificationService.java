package org.example.service;

import org.example.model.Notification;
import org.example.repository.NotificationRepository;

import java.util.List;

/**
 * Service de gestion des notifications.
 * Permet de creer, lire, marquer comme lues et supprimer les notifications utilisateur.
 */
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService() {
        this.repository = new NotificationRepository();
    }

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Cree une notification sans projet associe.
     * @param userId l'identifiant de l'utilisateur destinataire
     * @param message le contenu de la notification
     */
    public void createNotification(Long userId, String message) {
        createNotification(userId, message, null);
    }

    /**
     * Cree une notification avec un projet associe.
     * @param userId l'identifiant de l'utilisateur destinataire
     * @param message le contenu de la notification
     * @param projectId l'identifiant du projet associe (peut etre null)
     */
    public void createNotification(Long userId, String message, Long projectId) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        notif.setProjectId(projectId);
        repository.save(notif);
    }

    /**
     * Retourne toutes les notifications d'un utilisateur.
     * @param userId l'identifiant de l'utilisateur
     * @return la liste des notifications
     */
    public List<Notification> getUserNotifications(Long userId) {
        return repository.findByUser(userId);
    }

    /**
     * Retourne le nombre de notifications non lues d'un utilisateur.
     * @param userId l'identifiant de l'utilisateur
     * @return le nombre de notifications non lues
     */
    public int getUnreadCount(Long userId) {
        return repository.countUnread(userId);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     * @param userId l'identifiant de l'utilisateur
     */
    public void markAllAsRead(Long userId) {
        repository.markAllAsRead(userId);
    }

    /**
     * Inverse l'etat lu/non lu d'une notification.
     * @param id l'identifiant de la notification
     */
    public void toggleRead(Long id) {
        Notification notif = repository.findById(id);
        if (notif != null) {
            notif.setIsRead(!notif.isRead());
            repository.update(notif);
        }
    }

    /**
     * Supprime une notification par son identifiant.
     * @param id l'identifiant de la notification
     */
    public void deleteNotification(Long id) {
        repository.delete(id);
    }
}
