package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.UserStory;
import org.example.model.enums.Priority;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository d'acces aux donnees des user stories.
 * Gere les requetes JDBC pour la table user_story.
 */
public class UserStoryRepository {

    /**
     * Enregistre une nouvelle user story en base de donnees.
     * @param userStory la user story a enregistrer
     * @return l'identifiant genere
     */
    public Long save(UserStory userStory) {
        String sql = "INSERT INTO user_story (title, description, priority, project_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(userStory, stmt);

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new RuntimeException("No ID generated for user story");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user story: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne une user story par son identifiant.
     * @param id l'identifiant de la user story
     * @return la user story trouvee ou null
     */
    public UserStory findById(Long id) {
        String sql = "SELECT * FROM user_story WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUserStory(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user story: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne toutes les user stories.
     * @return la liste de toutes les user stories
     */
    public List<UserStory> findAll() {
        String sql = "SELECT * FROM user_story";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            List<UserStory> userStories = new ArrayList<>();
            while (rs.next()) {
                userStories.add(mapResultSetToUserStory(rs));
            }
            return userStories;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user stories: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne toutes les user stories d'un projet.
     * @param projectId l'identifiant du projet
     * @return la liste des user stories du projet
     */
    public List<UserStory> findByProject(Long projectId) {
        String sql = "SELECT * FROM user_story WHERE project_id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            var rs = stmt.executeQuery();
            List<UserStory> userStories = new ArrayList<>();
            while (rs.next()) {
                userStories.add(mapResultSetToUserStory(rs));
            }
            return userStories;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user stories by project: " + e.getMessage(), e);
        }
    }

    /**
     * Met a jour une user story existante.
     * @param userStory la user story avec les nouvelles valeurs
     */
    public void update(UserStory userStory) {
        String sql = "UPDATE user_story SET title = ?, description = ?, priority = ?, project_id = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            setParameters(userStory, stmt);
            stmt.setLong(5, userStory.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user story: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime une user story par son identifiant.
     * @param id l'identifiant de la user story
     */
    public void delete(Long id) {
        String sql = "DELETE FROM user_story WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user story: " + e.getMessage(), e);
        }
    }

    private void setParameters(UserStory userStory, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, userStory.getTitle());
        stmt.setString(2, userStory.getDescription());
        stmt.setString(3, userStory.getPriority().name());
        stmt.setLong(4, userStory.getProjectId());
    }

    private UserStory mapResultSetToUserStory(ResultSet rs) throws SQLException {
        return new UserStory(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                Priority.valueOf(rs.getString("priority")),
                rs.getLong("project_id")
        );
    }
}
