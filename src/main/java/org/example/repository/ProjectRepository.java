package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Project;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public Long save(Project project) {
        String sql = "INSERT INTO project (name, description, start_date, end_date, project_leader_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getStartDate());
            stmt.setString(4, project.getEndDate());
            stmt.setLong(5, project.getProjectLeaderId());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return project.getId();
            }
            throw new RuntimeException("No ID generated for project");

        } catch (SQLException e) {
            throw new RuntimeException("Error saving project: " + e.getMessage(), e);
        }
    }


    public Project findById(Long id) {
        String sql = "SELECT * FROM project where id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProject(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding project: " + e.getMessage(), e);
        }
    }

    public List<Project> findAll() {
        String sql = "SELECT * FROM project";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Project> projects = new ArrayList<>();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding projects: " + e.getMessage(), e);
        }
    }

    public List<Project> findByLeader(Long leaderId){
        String sql = "SELECT * FROM project where project_leader_id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, leaderId);
            ResultSet rs = stmt.executeQuery();
            List<Project> projects = new ArrayList<>();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding projects by leader: " + e.getMessage(), e);
        }
    }

    public void update(Project project){
        String sql = "UPDATE project SET name = ?, description = ?, start_date = ?, end_date = ?, project_leader_id = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)){
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getStartDate());
            stmt.setString(4, project.getEndDate());
            stmt.setLong(5, project.getProjectLeaderId());
            stmt.setLong(6, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException("Error updating project: " + e.getMessage(), e);
        }
    }

    public void delete(Long id){
        String sql = "DELETE FROM project WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)){
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException("Error deleting project: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a member to a project.
     */
    public void addMember(Long projectId, Long userId) {
        String sql = "INSERT INTO project_member (project_id, user_id) VALUES (?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding member to project: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a member from a project.
     */
    public void removeMember(Long projectId, Long userId) {
        String sql = "DELETE FROM project_member WHERE project_id = ? AND user_id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing member from project: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all members of a project.
     */
    public List<Long> findMemberIds(Long projectId) {
        String sql = "SELECT user_id FROM project_member WHERE project_id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            ResultSet rs = stmt.executeQuery();
            List<Long> memberIds = new ArrayList<>();
            while (rs.next()) {
                memberIds.add(rs.getLong("user_id"));
            }
            return memberIds;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding project members: " + e.getMessage(), e);
        }
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        return new Project(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("start_date"),
                rs.getString("end_date"),
                rs.getLong("project_leader_id")
        );
    }

}
