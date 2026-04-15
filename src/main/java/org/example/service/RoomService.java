package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.Room;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.RoomRepository;

import java.util.List;

/**
 * Service de gestion des salles.
 * Fournit les operations CRUD avec controle d'autorisation (admin uniquement).
 */
public class RoomService {

    private final RoomRepository roomRepository;

    /** Constructeur par defaut. */
    public RoomService() {
        this.roomRepository = new RoomRepository();
    }

    /**
     * Constructeur avec injection du repository.
     * @param roomRepository le repository des salles
     */
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Cree une nouvelle salle.
     * @param name le nom de la salle
     * @param capacity la capacite maximale
     * @return la salle creee
     */
    public Room createRoom(String name, int capacity) {
        if (!isAdmin()) {
            throw new AutorisationException("Only admins can create rooms");
        }
        Room room = new Room(null, name, capacity);
        Long generatedId = roomRepository.save(room);
        room.setId(generatedId);
        return room;
    }

    /**
     * Retourne une salle par son identifiant.
     * @param id l'identifiant de la salle
     * @return la salle trouvee
     * @throws NotFoundException si la salle n'existe pas
     */
    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id);
        if (room == null) throw new NotFoundException("Room not found");
        return room;
    }

    /**
     * Retourne toutes les salles.
     * @return la liste de toutes les salles
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Met a jour une salle existante.
     * @param room la salle avec les nouvelles valeurs
     */
    public void updateRoom(Room room) {
        if (!isAdmin()) {
            throw new AutorisationException("Only admins can update rooms");
        }
        roomRepository.update(room);
    }

    /**
     * Supprime une salle par son identifiant.
     * @param id l'identifiant de la salle
     */
    public void deleteRoom(Long id) {
        if (!isAdmin()) {
            throw new AutorisationException("Only admins can delete rooms");
        }
        roomRepository.delete(id);
    }

    private boolean isAdmin(){
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}

