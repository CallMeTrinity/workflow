package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.Room;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.RoomRepository;

import java.util.List;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService() {
        this.roomRepository = new RoomRepository();
    }

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String name, int capacity) {
        if (!isAdmin()) {
            throw new AutorisationException("Only admins can create rooms");
        }
        Room room = new Room(null, name, capacity);
        Long generatedId = roomRepository.save(room);
        room.setId(generatedId);
        return room;
    }

    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id);
        if (room == null) throw new NotFoundException("Room not found");
        return room;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public void updateRoom(Room room) {
        if (!isAdmin()) {
            throw new AutorisationException("Only admins can update rooms");
        }
        roomRepository.update(room);
    }

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

