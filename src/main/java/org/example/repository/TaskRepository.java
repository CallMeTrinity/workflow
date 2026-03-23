package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Task;
import org.example.model.enums.Priority;
import org.example.model.enums.Status;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public void save(Task task) {
        String sql = "INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            setParameters(task, stmt);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving task: " + e.getMessage(), e);
        }
    }

    public Task findById(Long id) {
        String sql = "SELECT * FROM task WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding task: " + e.getMessage(), e);
        }
    }

    public List<Task> findAll() {
        String sql = "SELECT * FROM task";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks: " + e.getMessage(), e);
        }
    }

    public List<Task> findByProject(Long projectId) {
        String sql = "SELECT * FROM task WHERE project_id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            var rs = stmt.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by project: " + e.getMessage(), e);
        }
    }

    public List<Task> findByAssignedUser(Long userId) {
        String sql = "SELECT * FROM task WHERE assigned_user_id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            var rs = stmt.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by assigned user: " + e.getMessage(), e);
        }
    }

    public void update(Task task) {
        String sql = "UPDATE task SET title = ?, description = ?, status = ?, priority = ?, deadline = ?, time_estimate = ?, assigned_user_id = ?, project_id = ?, user_story_id = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            setParameters(task, stmt);
            stmt.setLong(10, task.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task: " + e.getMessage(), e);
        }
    }

    private void setParameters(Task task, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, task.getTitle());
        stmt.setString(2, task.getDescription());
        stmt.setString(3, task.getStatus().name());
        stmt.setString(4, task.getPriority().name());
        stmt.setString(5, task.getDeadline());
        stmt.setInt(6, task.getTimeEstimate());
        if (task.getAssignedUserId() != null) {
            stmt.setLong(7, task.getAssignedUserId());
        } else {
            stmt.setNull(7, java.sql.Types.BIGINT);
        }
        stmt.setLong(8, task.getProjectId());
        if (task.getUserStoryId() != null) {
            stmt.setLong(9, task.getUserStoryId());
        } else {
            stmt.setNull(9, java.sql.Types.BIGINT);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM task WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task: " + e.getMessage(), e);
        }
    }

    private Task mapResultSetToTask(java.sql.ResultSet rs) throws SQLException {
        return new Task(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                Status.valueOf(rs.getString("status")),
                Priority.valueOf(rs.getString("priority")),
                rs.getString("deadline"),
                rs.getObject("time_estimate") != null ? rs.getInt("time_estimate") : null,
                rs.getObject("assigned_user_id") != null ? rs.getLong("assigned_user_id") : null,
                rs.getLong("project_id"),
                rs.getObject("user_story_id") != null ? rs.getLong("user_story_id") : null
        );
    }

}
