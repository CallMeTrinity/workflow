package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.User;
import org.example.model.enums.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public void save(User user) {
        String sql = "INSERT INTO users (last_name, first_name, mail, password, role) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {

            stmt.setString(1, user.getLastName());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getMail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt =
                     DatabaseConfig.getConnection().prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
    }


    public User findByMail(String mail) {
        String sql = "SELECT * FROM users WHERE mail = ?";

        try (PreparedStatement stmt =
                     DatabaseConfig.getConnection().prepareStatement(sql)) {

            stmt.setString(1, mail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";

        try (PreparedStatement stmt =
                     DatabaseConfig.getConnection().prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding users", e);
        }
    }

    public void update(User user) {
        String sql = "UPDATE users SET last_name = ?, first_name = ?, mail = ?, password = ?, role = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {

            stmt.setString(1, user.getLastName());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getMail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());
            stmt.setLong(6, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {

            stmt.setLong(1, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("mail"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role"))
        );
    }
}
