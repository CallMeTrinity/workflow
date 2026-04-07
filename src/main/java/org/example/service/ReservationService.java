package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.exception.ReservationConflictException;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ReservationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * @param userIds list des participants (IDs)
     * @param date     date de la réunion (ex: "2024-06-15")
     * @param durationMinutes durée minimale du créneau recherché en minutes (ex: 30)
     * @param roomId ID de la salle souhaitée (peut être null pour ignorer la salle)
     * @return une liste de créneaux disponibles, chaque créneau = [startMinutes, endMinutes] depuis minuit
     */
    public List<int[]> findAvailableSlots(List<Long> userIds, String date,
                                          int durationMinutes, Long roomId) {

        // 1. Collecter tous les intervalles occupés en minutes
        List<int[]> occupied = new ArrayList<>();

        // Créneaux occupés par les participants
        for (Long userId : userIds) {
            List<Reservation> reservations =
                    reservationRepository.findByParticipantAndDate(userId, date);
            for (Reservation r : reservations) {
                occupied.add(new int[]{
                        toMinutes(r.getStartTime()),
                        toMinutes(r.getEndTime())
                });
            }
        }

        // Créneaux occupés par la salle
        if (roomId != null) {
            List<Reservation> roomReservations =
                    reservationRepository.findByRoomAndDate(roomId, date);
            for (Reservation r : roomReservations) {
                occupied.add(new int[]{
                        toMinutes(r.getStartTime()),
                        toMinutes(r.getEndTime())
                });
            }
        }

        // 2. Fusionner les intervalles
        List<int[]> merged = mergeIntervals(occupied);

        // 3. Inverser → créneaux libres
        // Journée : 08:00 (480) → 20:00 (1200)
        List<int[]> free = invertIntervals(merged, 480, 1200);

        // 4. Filtrer par durée minimale
        return free.stream()
                .filter(slot -> slot[1] - slot[0] >= durationMinutes)
                .toList();
    }

    /**
     * Trouve les créneaux disponibles en proposant automatiquement les salles adaptées
     * au nombre de participants (capacité >= nbParticipants, la plus proche en taille).
     *
     * @return liste de [startMinutes, endMinutes, roomId]
     */
    public List<int[]> findAvailableSlotsWithAutoRoom(List<Long> userIds, String date,
                                                       int durationMinutes, List<Room> allRooms) {
        int nbParticipants = userIds.size();

        // Trier les salles par capacité croissante, ne garder que celles assez grandes
        List<Room> candidates = allRooms.stream()
                .filter(r -> r.getCapacity() >= nbParticipants)
                .sorted((a, b) -> Integer.compare(a.getCapacity(), b.getCapacity()))
                .toList();

        // Créneaux occupés par les participants (commun à toutes les salles)
        List<int[]> participantOccupied = new ArrayList<>();
        for (Long userId : userIds) {
            List<Reservation> reservations =
                    reservationRepository.findByParticipantAndDate(userId, date);
            for (Reservation r : reservations) {
                participantOccupied.add(new int[]{
                        toMinutes(r.getStartTime()),
                        toMinutes(r.getEndTime())
                });
            }
        }

        List<int[]> results = new ArrayList<>();

        for (Room room : candidates) {
            // Fusionner occupations participants + occupations de cette salle
            List<int[]> occupied = new ArrayList<>(participantOccupied);
            List<Reservation> roomReservations =
                    reservationRepository.findByRoomAndDate(room.getId(), date);
            for (Reservation r : roomReservations) {
                occupied.add(new int[]{
                        toMinutes(r.getStartTime()),
                        toMinutes(r.getEndTime())
                });
            }

            List<int[]> merged = mergeIntervals(occupied);
            List<int[]> free = invertIntervals(merged, 480, 1200);

            for (int[] slot : free) {
                if (slot[1] - slot[0] >= durationMinutes) {
                    results.add(new int[]{slot[0], slot[1], room.getId().intValue()});
                }
            }
        }

        return results;
    }

    public List<Reservation> getReservationsForUser(Long userId, String dateFrom, String dateTo) {
        return reservationRepository.findForUserInRange(userId, dateFrom, dateTo);
    }

    public Map<Long, String> getMyStatusForReservations(Long userId, String dateFrom, String dateTo) {
        return reservationRepository.findParticipantStatusesForUser(userId, dateFrom, dateTo);
    }

    public void declineReservation(Long reservationId) {
        Long userId = SessionManager.getCurrentUser().getId();
        reservationRepository.updateParticipantStatus(reservationId, userId, "declined");
    }

    public void acceptReservation(Long reservationId) {
        Long userId = SessionManager.getCurrentUser().getId();
        reservationRepository.updateParticipantStatus(reservationId, userId, "accepted");
    }

    /**
     * Returns IDs of participants who already have a reservation overlapping the given time slot.
     */
    public List<Long> findConflictingUserIds(List<Long> participantIds, String date,
                                              String startTime, String endTime) {
        List<Long> conflicting = new ArrayList<>();
        for (Long userId : participantIds) {
            List<Reservation> existing = reservationRepository.findForUserInRange(userId, date, date);
            for (Reservation r : existing) {
                if (timesOverlap(r.getStartTime(), r.getEndTime(), startTime, endTime)) {
                    conflicting.add(userId);
                    break;
                }
            }
        }
        return conflicting;
    }

    public void addParticipant(Long reservationId, Long userId) {
        reservationRepository.addParticipant(reservationId, userId);
    }

    public void removeParticipant(Long reservationId, Long userId) {
        reservationRepository.removeParticipant(reservationId, userId);
    }

    public List<Long> getParticipantIds(Long reservationId) {
        return reservationRepository.findParticipantIds(reservationId);
    }

    public void updateReservation(Reservation reservation) {
        reservationRepository.update(reservation);
    }

    private List<int[]> mergeIntervals(List<int[]> intervals) {
        if (intervals.isEmpty()) return intervals;

        // Trier par heure de début
        intervals.sort((a, b) -> a[0] - b[0]);

        List<int[]> merged = new ArrayList<>();
        int[] current = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            int[] next = intervals.get(i);
            if (next[0] <= current[1]) {
                // Chevauchement — étendre l'intervalle courant
                current[1] = Math.max(current[1], next[1]);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    private List<int[]> invertIntervals(List<int[]> occupied, int dayStart, int dayEnd) {
        List<int[]> free = new ArrayList<>();
        int cursor = dayStart;

        for (int[] interval : occupied) {
            if (cursor < interval[0]) {
                free.add(new int[]{cursor, interval[0]});
            }
            cursor = Math.max(cursor, interval[1]);
        }

        if (cursor < dayEnd) {
            free.add(new int[]{cursor, dayEnd});
        }
        return free;
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
