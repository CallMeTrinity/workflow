package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.User;
import org.example.model.enums.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for CRUD operations on users.
 */
public class UserRepository {

    /**
     * Saves a new user in the database.
     * @param user the user to save
     */
    public Long save(User user) {
        String sql = "INSERT INTO users (last_name, first_name, mail, password, role, username) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getLastName());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getMail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getUsername());
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new RuntimeException("No ID generated for user");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a user by their ID.
     * @param id the user ID
     * @return the user, or null if not found
     */
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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


    /**
     * Finds a user by their email address.
     * @param mail the email address
     * @return the user, or null if not found
     */
    public User findByMail(String mail) {
        String sql = "SELECT * FROM users WHERE mail = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, mail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by mail", e);
        }
    }

    /**
     * Returns all users in the database.
     * @return list of all users
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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

    /**
     * Updates an existing user in the database.
     * @param user the user to update (must have a valid id)
     */
    public void update(User user) {
        String sql = "UPDATE users SET last_name = ?, first_name = ?, mail = ?, password = ?, role = ?, username = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getLastName());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getMail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getUsername());
            stmt.setLong(7, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a user by their ID.
     * @param id the ID of the user to delete
     */
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    /**
     * Maps a ResultSet row to a User object.
     * @param rs the ResultSet positioned on a row
     * @return the corresponding User
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("mail"),
                rs.getString("password"),
                rs.getString("username"),
                Role.valueOf(rs.getString("role"))
        );
    }

    public void updateUsername(Long id, String username) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
