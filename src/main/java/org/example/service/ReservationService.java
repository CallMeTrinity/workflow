package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.exception.ReservationConflictException;
import org.example.model.Reservation;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ReservationRepository;

import java.util.List;

/**
 * Service for managing reservations.
 */
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
    }

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Creates a new reservation after checking for time conflicts.
     */
    public Reservation createReservation(String title, String description, String date,
                                         String startTime, String endTime,
                                         Long roomId, Long projectId, Long organizerId) {
        List<Reservation> existing = reservationRepository.findByRoomAndDate(roomId, date);
        if (hasConflict(existing, startTime, endTime)) {
            throw new ReservationConflictException("Time slot is already booked for this room");
        }

        int start = toMinutes(startTime);
        int end = toMinutes(endTime);
        if (end <= start) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Reservation reservation = new Reservation(null, title, description, date,
                startTime, endTime, roomId, projectId, organizerId);
        Long generatedId = reservationRepository.save(reservation);
        reservation.setId(generatedId);
        return reservation;
    }

    /**
     * Returns a reservation by its ID.
     *
     * @throws NotFoundException if no reservation matches the given ID
     */
    public Reservation getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) throw new NotFoundException("Reservation not found");
        return reservation;
    }

    /**
     * Returns all reservations.
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Returns all reservations for a given room on a given date.
     */
    public List<Reservation> getReservationsByRoom(Long roomId, String date) {
        return reservationRepository.findByRoomAndDate(roomId, date);
    }

    /**
     * Cancels a reservation. Only admins or the organizer can cancel.
     */
    public void cancelReservation(Long id) {
        User currentUser = SessionManager.getCurrentUser();
        Reservation reservation = getReservationById(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOrganizer = reservation.getOrganizerId().equals(currentUser.getId());

        if (!isAdmin && !isOrganizer) {
            throw new AutorisationException("Only admins or the organizer can cancel a reservation");
        }
        reservationRepository.delete(id);
    }

    private boolean hasConflict(List<Reservation> existing, String startTime, String endTime) {
        for (Reservation r : existing) {
            if (timesOverlap(r.getStartTime(), r.getEndTime(), startTime, endTime)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convertit "HH:MM" en minutes depuis minuit.
     * Ex: "14:30" -> 870
     */
    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /**
     * Convertit des minutes depuis minuit en "HH:MM".
     * Ex: 870 -> "14:30"
     */
    private String toTimeString(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private boolean timesOverlap(String start1, String end1, String start2, String end2) {
        int s1 = toMinutes(start1);
        int e1 = toMinutes(end1);
        int s2 = toMinutes(start2);
        int e2 = toMinutes(end2);
        return s1 < e2 && s2 < e1;
    }
}
