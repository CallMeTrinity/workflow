package org.example.repository;

import org.example.config.DatabaseConfig;
import org.example.model.Project;
import org.example.model.Room;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository d'acces aux donnees des salles.
 * Gere les requetes JDBC pour la table room.
 */
public class RoomRepository {

    /**
     * Enregistre une nouvelle salle en base de donnees.
     * @param room la salle a enregistrer
     * @return l'identifiant genere
     */
    public Long save(Room room) {
        String sql ="INSERT INTO room (name, capacity) VALUES (?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, room.getName());
            stmt.setInt(2, room.getCapacity());
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new RuntimeException("No ID generated for room");

        } catch (SQLException e) {
            throw new RuntimeException("Error saving room: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne une salle par son identifiant.
     * @param id l'identifiant de la salle
     * @return la salle trouvee ou null
     */
    public Room findById(Long id){
        String sql = "SELECT * FROM room where id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRoom(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne toutes les salles.
     * @return la liste de toutes les salles
     */
    public List<Room> findAll(){
        String sql = "SELECT * FROM room";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();
            List<Room> rooms = new ArrayList<>();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            return rooms;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Met a jour une salle existante.
     * @param room la salle avec les nouvelles valeurs
     */
    public void update(Room room) {
        String sql = "UPDATE room SET name = ?, capacity = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)){
            stmt.setString(1, room.getName());
            stmt.setInt(2, room.getCapacity());
            stmt.setLong(3, room.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating room: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime une salle par son identifiant.
     * @param id l'identifiant de la salle
     */
    public void delete(Long id) {
        String sql = "DELETE FROM room WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)){
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting room: " + e.getMessage(), e);
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("capacity")
        );
    }
}
