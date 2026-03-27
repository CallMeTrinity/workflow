package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Reservation;
import org.example.model.Room;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {
    public Long save(Reservation reservation) {

        String sql = "INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
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

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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

    /**
     * Returns all reservations for a given participant on a given date.
     */
    public List<Reservation> findByParticipantAndDate(Long userId, String date) {
        String sql = """
            SELECT r.* FROM reservation r
            JOIN participants_reservation pr ON r.id = pr.reservation_id
            WHERE pr.user_id = ? AND r.date = ?
            """;

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, date);
            ResultSet rs = stmt.executeQuery();
            List<Reservation> reservations = new ArrayList<>();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
            return reservations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations by participant and date: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a participant to a reservation.
     */
    public void addParticipant(Long reservationId, Long userId) {
        String sql = "INSERT INTO participants_reservation (reservation_id, user_id) VALUES (?, ?)";

        try (var stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, reservationId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding participant: " + e.getMessage(), e);
        }
    }

    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservation";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            setParameters(reservation, stmt);
            stmt.setLong(9, reservation.getId());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
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
