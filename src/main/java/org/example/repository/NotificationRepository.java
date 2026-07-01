package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository d'acces aux donnees des notifications.
 * Gere les requetes JDBC pour la table notification.
 */
public class NotificationRepository {

    /**
     * Retourne toutes les notifications d'un utilisateur, triees par identifiant decroissant.
     * @param userId l'identifiant de l'utilisateur
     * @return la liste des notifications
     */
    public List<Notification> findByUser(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY id DESC";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding notifications for user", e);
        }
    }

    /**
     * Retourne une notification par son identifiant.
     * @param id l'identifiant de la notification
     * @return la notification trouvee ou null
     */
    public Notification findById(Long id) {
        String sql = "SELECT * FROM notification WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding notification", e);
        }
    }

    /**
     * Compte le nombre de notifications non lues d'un utilisateur.
     * @param userId l'identifiant de l'utilisateur
     * @return le nombre de notifications non lues
     */
    public int countUnread(Long userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread notifications", e);
        }
    }

    /**
     * Enregistre une nouvelle notification en base de donnees.
     * @param notification la notification a enregistrer
     */
    public void save(Notification notification) {
        String sql = "INSERT INTO notification (message, user_id, is_read, project_id) VALUES (?, ?, 0, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, notification.getMessage());
            stmt.setLong(2, notification.getUserId());
            if (notification.getProjectId() != null) {
                stmt.setLong(3, notification.getProjectId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving notification", e);
        }
    }

    /**
     * Met a jour une notification existante.
     * @param notification la notification avec les nouvelles valeurs
     */
    public void update(Notification notification) {
        String sql = "UPDATE notification SET message = ?, user_id = ?, is_read = ?, project_id = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, notification.getMessage());
            stmt.setLong(2, notification.getUserId());
            stmt.setBoolean(3, notification.isRead());
            if (notification.getProjectId() != null) {
                stmt.setLong(4, notification.getProjectId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setLong(5, notification.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating notification", e);
        }
    }

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues.
     * @param userId l'identifiant de l'utilisateur
     */
    public void markAllAsRead(Long userId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking notifications as read", e);
        }
    }

    /**
     * Supprime une notification par son identifiant.
     * @param id l'identifiant de la notification
     */
    public void delete(Long id) {
        String sql = "DELETE FROM notification WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting notification", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Long projectId = rs.getObject("project_id") != null ? rs.getLong("project_id") : null;
        return new Notification(
                rs.getLong("id"),
                rs.getString("message"),
                rs.getLong("user_id"),
                rs.getBoolean("is_read"),
                projectId
        );
    }
}
