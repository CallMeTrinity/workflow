package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Reservation;
import org.example.model.Room;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository d'acces aux donnees des reservations.
 * Gere les requetes JDBC pour les tables reservation et participants_reservation.
 */
public class ReservationRepository {
    /**
     * Enregistre une nouvelle reservation en base de donnees.
     * @param reservation la reservation a enregistrer
     * @return l'identifiant genere
     */
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

    /**
     * Retourne une reservation par son identifiant.
     * @param id l'identifiant de la reservation
     * @return la reservation trouvee ou null
     */
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

    /**
     * Retourne toutes les reservations.
     * @return la liste de toutes les reservations
     */
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
     * Returns all reservations where the user is organizer or participant, within a date range.
     */
    public List<Reservation> findForUserInRange(Long userId, String dateFrom, String dateTo) {
        String sql = """
                SELECT DISTINCT r.* FROM reservation r
                LEFT JOIN participants_reservation pr ON r.id = pr.reservation_id
                WHERE (r.organizer_id = ? OR pr.user_id = ?)
                  AND r.date >= ? AND r.date <= ?
                ORDER BY r.date, r.start_time
                """;
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.setString(3, dateFrom);
            stmt.setString(4, dateTo);
            ResultSet rs = stmt.executeQuery();
            List<Reservation> result = new ArrayList<>();
            while (rs.next()) result.add(mapResultSetToReservation(rs));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations for user in range: " + e.getMessage(), e);
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

    /**
     * Met a jour une reservation existante.
     * @param reservation la reservation avec les nouvelles valeurs
     */
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

    /**
     * Supprime une reservation par son identifiant.
     * @param id l'identifiant de la reservation
     */
    public void delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error deleting reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the list of participant user IDs for a given reservation.
     */
    public List<Long> findParticipantIds(Long reservationId) {
        String sql = "SELECT user_id FROM participants_reservation WHERE reservation_id = ?";
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getLong("user_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding participants: " + e.getMessage(), e);
        }
        return ids;
    }

    /**
     * Updates the participation status for a user on a reservation.
     */
    public void updateParticipantStatus(Long reservationId, Long userId, String status) {
        String sql = "UPDATE participants_reservation SET status = ? WHERE reservation_id = ? AND user_id = ?";
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, reservationId);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating participant status: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a map of reservationId -> status for all reservations where the user is a participant,
     * within a date range.
     */
    public Map<Long, String> findParticipantStatusesForUser(Long userId, String dateFrom, String dateTo) {
        String sql = """
                SELECT pr.reservation_id, pr.status FROM participants_reservation pr
                JOIN reservation r ON r.id = pr.reservation_id
                WHERE pr.user_id = ? AND r.date >= ? AND r.date <= ?
                """;
        Map<Long, String> result = new HashMap<>();
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, dateFrom);
            stmt.setString(3, dateTo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getLong("reservation_id"), rs.getString("status"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding participant statuses: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Removes a participant from a reservation.
     */
    public void removeParticipant(Long reservationId, Long userId) {
        String sql = "DELETE FROM participants_reservation WHERE reservation_id = ? AND user_id = ?";
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, reservationId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing participant: " + e.getMessage(), e);
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
