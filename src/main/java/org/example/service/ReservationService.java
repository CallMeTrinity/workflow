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

    private boolean timesOverlap(String start1, String end1, String start2, String end2) {
        return start1.compareTo(end2) < 0 && start2.compareTo(end1) < 0;
    }
}
