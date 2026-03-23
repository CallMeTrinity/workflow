package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Project;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public void save(Project project) {
        String sql = "INSERT INTO project (name, description, start_date, end_date, project_leader_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getStartDate());
            stmt.setString(4, project.getEndDate());
            stmt.setLong(5, project.getProjectLeaderId());

            stmt.executeUpdate();

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
