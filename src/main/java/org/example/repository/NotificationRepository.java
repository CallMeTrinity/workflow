package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    public List<Notification> findByUser(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY id DESC";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Notification findById(Long id) {
        String sql = "SELECT * FROM notification WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countUnread(Long userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

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
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

    public void markAllAsRead(Long userId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM notification WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
