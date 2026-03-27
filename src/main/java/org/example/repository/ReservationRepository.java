package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Reservation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ReservationRepository {
    public Long save(Reservation reservation) {

        String sql = "INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            setParameters(reservation, stmt);

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new RuntimeException("No ID generated for reservation");

        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error saving reservation: " + e.getMessage(), e);
        }
    }

    public Reservation findById(Long id) {
        String sql = "SELECT * FROM reservation WHERE id = ?";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
            return null;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error finding reservation: " + e.getMessage(), e);
        }
    }

    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservation";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Reservation> reservations = new java.util.ArrayList<>();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
            return reservations;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error finding reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all reservations for a given room on a given date.
     */
    public List<Reservation> findByRoomAndDate(Long roomId, String date) {
        String sql = "SELECT * FROM reservation WHERE room_id = ? AND date = ?";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, roomId);
            stmt.setString(2, date);
            ResultSet rs = stmt.executeQuery();
            List<Reservation> reservations = new java.util.ArrayList<>();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
            return reservations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations by room and date: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all reservations for a given organizer.
     */
    public List<Reservation> findByOrganizer(Long organizerId) {
        String sql = "SELECT * FROM reservation WHERE organizer_id = ?";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, organizerId);
            ResultSet rs = stmt.executeQuery();
            List<Reservation> reservations = new java.util.ArrayList<>();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
            return reservations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations by organizer: " + e.getMessage(), e);
        }
    }

    public void update(Reservation reservation) {
        String sql = "UPDATE reservation SET title = ?, description = ?, date = ?, start_time = ?, end_time = ?, room_id = ?, project_id = ?, organizer_id = ? WHERE id = ?";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            setParameters(reservation, stmt);
            stmt.setLong(9, reservation.getId());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error deleting reservation: " + e.getMessage(), e);
        }
    }

    private void setParameters(Reservation reservation, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, reservation.getTitle());
        stmt.setString(2, reservation.getDescription());
        stmt.setString(3, reservation.getDate());
        stmt.setString(4, reservation.getStartTime());
        stmt.setString(5, reservation.getEndTime());
        stmt.setLong(6, reservation.getRoomId());
        if (reservation.getProjectId() != null) {
            stmt.setLong(7, reservation.getProjectId());
        } else {
            stmt.setNull(7, java.sql.Types.BIGINT);
        }
        stmt.setLong(8, reservation.getOrganizerId());
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws java.sql.SQLException {
        return new Reservation(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("date"),
                rs.getString("start_time"),
                rs.getString("end_time"),
                rs.getLong("room_id"),
                rs.getObject("project_id") != null ? rs.getLong("project_id") : null,
                rs.getLong("organizer_id")
        );
    }

}
