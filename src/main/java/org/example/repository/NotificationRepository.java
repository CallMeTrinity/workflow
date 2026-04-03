package org.example.repository;

import org.example.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private Connection connection;

    public NotificationRepository() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:project_management.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Long save(Notification notification) {
        String sql = "INSERT INTO notification (message, user_id, is_read) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, notification.getMessage());
            stmt.setLong(2, notification.getUserId());
            stmt.setBoolean(3, notification.isRead());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public List<Notification> findByUser(Long userId) {
        List<Notification> list = new ArrayList<>();

        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add (new Notification(
                        rs.getLong("id"),
                        rs.getString("message"),
                        rs.getLong("user_id"),
                        rs.getBoolean("is_read")
                ));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public void update(Notification notification) {
        String sql = "UPDATE notification SET message = ?, user_id = ?, is_read = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, notification.getMessage());
            stmt.setLong(2, notification.getUserId());
            stmt.setBoolean(3, notification.isRead());
            stmt.setLong(4, notification.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM notification WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}