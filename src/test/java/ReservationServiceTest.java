import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.InvalidDateInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ReservationConflictException;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ReservationRepository;
import org.example.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User adminUser;
    private User leaderUser;
    private User memberUser;
    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        adminUser  = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN, null);
        leaderUser = new User(2L, "Leader", "John", "leader@test.com", "hash", Role.PROJECT_LEADER, null);
        memberUser = new User(3L, "Member", "Jane", "member@test.com", "hash", Role.MEMBER, null);

        sampleReservation = new Reservation(
                1L, "Réunion", "Desc", "2024-06-01",
                "09:00", "10:00", 1L, null, 3L
        );
    }

    // --- createReservation ---

    @Test
    void shouldCreateReservationWithNoConflict() {
        SessionManager.setCurrentUser(adminUser);
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01")).thenReturn(List.of());
        when(reservationRepository.save(any())).thenReturn(1L);

        Reservation result = reservationService.createReservation(
                "Réunion", "Desc", "2024-06-01", "09:00", "10:00", 1L, null, 1L
        );

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("09:00", result.getStartTime());
    }

    @Test
    void shouldThrowConflictWhenOverlapExact() {
        // Réservation existante : 09:00 - 10:00
        // Nouvelle : 09:00 - 10:00 → conflit
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));

        assertThrows(ReservationConflictException.class, () ->
                reservationService.createReservation(
                        "Conflit", "Desc", "2024-06-01", "09:00", "10:00", 1L, null, 1L
                )
        );
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenOverlapPartialStart() {
        // Existante : 09:00 - 10:00
        // Nouvelle : 08:30 - 09:30 → chevauche le début
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));

        assertThrows(ReservationConflictException.class, () ->
                reservationService.createReservation(
                        "Conflit", "Desc", "2024-06-01", "08:30", "09:30", 1L, null, 1L
                )
        );
    }

    @Test
    void shouldThrowConflictWhenOverlapPartialEnd() {
        // Existante : 09:00 - 10:00
        // Nouvelle : 09:30 - 10:30 → chevauche la fin
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));

        assertThrows(ReservationConflictException.class, () ->
                reservationService.createReservation(
                        "Conflit", "Desc", "2024-06-01", "09:30", "10:30", 1L, null, 1L
                )
        );
    }

    @Test
    void shouldThrowConflictWhenInsideExisting() {
        // Existante : 09:00 - 10:00
        // Nouvelle : 09:15 - 09:45 → à l'intérieur
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));

        assertThrows(ReservationConflictException.class, () ->
                reservationService.createReservation(
                        "Conflit", "Desc", "2024-06-01", "09:15", "09:45", 1L, null, 1L
                )
        );
    }

    @Test
    void shouldNotConflictWhenAfter() {
        // Existante : 09:00 - 10:00
        // Nouvelle : 10:00 - 11:00 → pas de conflit
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));
        when(reservationRepository.save(any())).thenReturn(2L);

        assertDoesNotThrow(() ->
                reservationService.createReservation(
                        "Après", "Desc", "2024-06-01", "10:00", "11:00", 1L, null, 1L
                )
        );
    }

    @Test
    void shouldNotConflictWhenBefore() {
        // Existante : 09:00 - 10:00
        // Nouvelle : 07:00 - 09:00 → pas de conflit
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));
        when(reservationRepository.save(any())).thenReturn(3L);

        assertDoesNotThrow(() ->
                reservationService.createReservation(
                        "Avant", "Desc", "2024-06-01", "07:00", "09:00", 1L, null, 1L
                )
        );
    }

    @Test
    void shouldNotConflictOnDifferentDate() {
        // Même salle mais date différente → pas de conflit
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-02"))
                .thenReturn(List.of());
        when(reservationRepository.save(any())).thenReturn(4L);

        assertDoesNotThrow(() ->
                reservationService.createReservation(
                        "Autre jour", "Desc", "2024-06-02", "09:00", "10:00", 1L, null, 1L
                )
        );
    }

    // --- getReservationById ---

    @Test
    void shouldReturnReservationById() {
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        Reservation result = reservationService.getReservationById(1L);

        assertEquals("Réunion", result.getTitle());
    }

    @Test
    void shouldThrowNotFoundIfReservationMissing() {
        when(reservationRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> reservationService.getReservationById(99L));
    }

    // --- cancelReservation ---

    @Test
    void adminShouldCancelAnyReservation() {
        SessionManager.setCurrentUser(adminUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        reservationService.cancelReservation(1L);

        verify(reservationRepository).delete(1L);
    }

    @Test
    void organizerShouldCancelOwnReservation() {
        SessionManager.setCurrentUser(memberUser); // memberUser.id = 3L = organizerId
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        reservationService.cancelReservation(1L);

        verify(reservationRepository).delete(1L);
    }

    @Test
    void nonOrganizerShouldNotCancelReservation() {
        User otherUser = new User(99L, "Other", "User", "other@test.com", "hash", Role.MEMBER, null);
        SessionManager.setCurrentUser(otherUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        assertThrows(AutorisationException.class, () ->
                reservationService.cancelReservation(1L)
        );
        verify(reservationRepository, never()).delete(any());
    }

    // --- getReservationsByRoom ---

    @Test
    void shouldReturnReservationsByRoomAndDate() {
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(sampleReservation));

        List<Reservation> result = reservationService.getReservationsByRoom(1L, "2024-06-01");

        assertEquals(1, result.size());
        assertEquals("Réunion", result.get(0).getTitle());
    }

    // --- createReservation with invalid time ---

    @Test
    void shouldThrowIfEndTimeBeforeStartTime() {
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.createReservation(
                        "Bad", "Desc", "2024-06-01", "10:00", "09:00", 1L, null, 1L
                )
        );
        // La validation doit avoir lieu avant toute requete en base
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowIfEndTimeEqualsStartTime() {
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.createReservation(
                        "Bad", "Desc", "2024-06-01", "10:00", "10:00", 1L, null, 1L
                )
        );
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowIfTimeFormatInvalid() {
        assertThrows(InvalidDateInputException.class, () ->
                reservationService.createReservation(
                        "Bad", "Desc", "2024-06-01", "9h00", "10:00", 1L, null, 1L
                )
        );
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowIfTimeIsNull() {
        assertThrows(InvalidDateInputException.class, () ->
                reservationService.createReservation(
                        "Bad", "Desc", "2024-06-01", null, "10:00", 1L, null, 1L
                )
        );
        verifyNoInteractions(reservationRepository);
    }

    // --- addParticipant ---

    @Test
    void adminShouldAddParticipant() {
        SessionManager.setCurrentUser(adminUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        reservationService.addParticipant(1L, 5L);

        verify(reservationRepository).addParticipant(1L, 5L);
    }

    @Test
    void organizerShouldAddParticipant() {
        SessionManager.setCurrentUser(memberUser); // memberUser.id = 3L = organizerId
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        reservationService.addParticipant(1L, 5L);

        verify(reservationRepository).addParticipant(1L, 5L);
    }

    @Test
    void nonOrganizerNonAdminShouldNotAddParticipant() {
        User otherUser = new User(99L, "Other", "User", "other@test.com", "hash", Role.MEMBER, null);
        SessionManager.setCurrentUser(otherUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        assertThrows(AutorisationException.class, () ->
                reservationService.addParticipant(1L, 5L)
        );
        verify(reservationRepository, never()).addParticipant(anyLong(), anyLong());
    }

    // --- removeParticipant ---

    @Test
    void adminShouldRemoveParticipant() {
        SessionManager.setCurrentUser(adminUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        reservationService.removeParticipant(1L, 5L);

        verify(reservationRepository).removeParticipant(1L, 5L);
    }

    @Test
    void nonOrganizerShouldNotRemoveParticipant() {
        User otherUser = new User(99L, "Other", "User", "other@test.com", "hash", Role.MEMBER, null);
        SessionManager.setCurrentUser(otherUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        assertThrows(AutorisationException.class, () ->
                reservationService.removeParticipant(1L, 5L)
        );
        verify(reservationRepository, never()).removeParticipant(anyLong(), anyLong());
    }

    // --- updateReservation ---

    @Test
    void organizerShouldUpdateReservation() {
        SessionManager.setCurrentUser(memberUser); // organizer
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        Reservation updated = new Reservation(1L, "Updated", "Desc", "2024-06-01",
                "09:00", "11:00", 1L, null, 3L);
        reservationService.updateReservation(updated);

        verify(reservationRepository).update(updated);
    }

    @Test
    void nonOrganizerShouldNotUpdateReservation() {
        User otherUser = new User(99L, "Other", "User", "other@test.com", "hash", Role.MEMBER, null);
        SessionManager.setCurrentUser(otherUser);
        when(reservationRepository.findById(1L)).thenReturn(sampleReservation);

        Reservation updated = new Reservation(1L, "Updated", "Desc", "2024-06-01",
                "09:00", "11:00", 1L, null, 3L);

        assertThrows(AutorisationException.class, () ->
                reservationService.updateReservation(updated)
        );
        verify(reservationRepository, never()).update(any());
    }

    // --- declineReservation / acceptReservation ---

    @Test
    void declineReservationShouldUpdateStatus() {
        SessionManager.setCurrentUser(memberUser);

        reservationService.declineReservation(1L);

        verify(reservationRepository).updateParticipantStatus(1L, 3L, "declined");
    }

    @Test
    void acceptReservationShouldUpdateStatus() {
        SessionManager.setCurrentUser(memberUser);

        reservationService.acceptReservation(1L);

        verify(reservationRepository).updateParticipantStatus(1L, 3L, "accepted");
    }

    // --- findConflictingUserIds ---

    @Test
    void findConflictingUserIdsShouldReturnConflictingUsers() {
        Reservation overlap = new Reservation(2L, "Other", "Desc", "2024-06-01",
                "09:30", "10:30", 2L, null, 1L);
        when(reservationRepository.findForUserInRange(5L, "2024-06-01", "2024-06-01"))
                .thenReturn(List.of(overlap));
        when(reservationRepository.findForUserInRange(6L, "2024-06-01", "2024-06-01"))
                .thenReturn(List.of());

        List<Long> result = reservationService.findConflictingUserIds(
                List.of(5L, 6L), "2024-06-01", "09:00", "10:00"
        );

        assertEquals(1, result.size());
        assertTrue(result.contains(5L));
    }

    @Test
    void findConflictingUserIdsWithNoConflictsShouldReturnEmpty() {
        Reservation noOverlap = new Reservation(2L, "Other", "Desc", "2024-06-01",
                "11:00", "12:00", 2L, null, 1L);
        when(reservationRepository.findForUserInRange(5L, "2024-06-01", "2024-06-01"))
                .thenReturn(List.of(noOverlap));

        List<Long> result = reservationService.findConflictingUserIds(
                List.of(5L), "2024-06-01", "09:00", "10:00"
        );

        assertTrue(result.isEmpty());
    }

    // --- getParticipantIds ---

    @Test
    void getParticipantIdsShouldReturnList() {
        when(reservationRepository.findParticipantIds(1L)).thenReturn(List.of(3L, 5L));

        List<Long> result = reservationService.getParticipantIds(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(3L));
        assertTrue(result.contains(5L));
    }

    // --- checkOrganizerOrAdmin with null reservation ---

    @Test
    void addParticipantShouldThrowWhenReservationNotFound() {
        SessionManager.setCurrentUser(adminUser);
        when(reservationRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () ->
                reservationService.addParticipant(99L, 5L)
        );
    }

    // --- findAvailableSlots ---

    @Test
    void findAvailableSlotsShouldReturnFreeSlots() {
        when(reservationRepository.findByParticipantAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(
                        new Reservation(1L, "R1", "", "2024-06-01", "09:00", "10:00", 1L, null, 1L)
                ));
        when(reservationRepository.findByRoomAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(
                        new Reservation(2L, "R2", "", "2024-06-01", "14:00", "15:00", 1L, null, 1L)
                ));

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(1L), "2024-06-01", 30, 1L
        );

        assertFalse(slots.isEmpty());
        for (int[] slot : slots) {
            assertTrue(slot[1] - slot[0] >= 30);
        }
    }

    @Test
    void findAvailableSlotsShouldWorkWithoutRoom() {
        when(reservationRepository.findByParticipantAndDate(1L, "2024-06-01"))
                .thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(1L), "2024-06-01", 60, null
        );

        assertFalse(slots.isEmpty());
    }

    @Test
    void findAvailableSlotsShouldReturnEmptyWhenFullyBooked() {
        when(reservationRepository.findByParticipantAndDate(1L, "2024-06-01"))
                .thenReturn(List.of(
                        new Reservation(1L, "R", "", "2024-06-01", "08:00", "20:00", 1L, null, 1L)
                ));

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(1L), "2024-06-01", 30, null
        );

        assertTrue(slots.isEmpty());
    }

    // --- findAvailableSlotsWithAutoRoom ---

    @Test
    void findAvailableSlotsWithAutoRoomShouldReturnSlotsWithRoomId() {
        when(reservationRepository.findByParticipantAndDate(1L, "2024-06-01"))
                .thenReturn(List.of());
        when(reservationRepository.findByRoomAndDate(10L, "2024-06-01"))
                .thenReturn(List.of());

        Room room = new Room(10L, "Salle A", 5);
        List<int[]> slots = reservationService.findAvailableSlotsWithAutoRoom(
                List.of(1L), "2024-06-01", 60, List.of(room)
        );

        assertFalse(slots.isEmpty());
        assertEquals(10, slots.get(0)[2]);
    }

    @Test
    void findAvailableSlotsWithAutoRoomShouldSkipTooSmallRooms() {
        when(reservationRepository.findByParticipantAndDate(anyLong(), eq("2024-06-01")))
                .thenReturn(List.of());
        when(reservationRepository.findByRoomAndDate(20L, "2024-06-01"))
                .thenReturn(List.of());

        Room tooSmall = new Room(10L, "Petite", 1);
        Room bigEnough = new Room(20L, "Grande", 10);

        List<int[]> slots = reservationService.findAvailableSlotsWithAutoRoom(
                List.of(1L, 2L, 3L), "2024-06-01", 60, List.of(tooSmall, bigEnough)
        );

        assertFalse(slots.isEmpty());
        for (int[] slot : slots) {
            assertEquals(20, slot[2]);
        }
    }

    // --- getAllReservations ---

    @Test
    void getAllReservationsShouldReturnList() {
        when(reservationRepository.findAll()).thenReturn(List.of(sampleReservation));

        List<Reservation> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
    }

    // --- getReservationsForUser ---

    @Test
    void getReservationsForUserShouldReturnList() {
        when(reservationRepository.findForUserInRange(1L, "2024-06-01", "2024-06-30"))
                .thenReturn(List.of(sampleReservation));

        List<Reservation> result = reservationService.getReservationsForUser(1L, "2024-06-01", "2024-06-30");

        assertEquals(1, result.size());
    }

    // --- getMyStatusForReservations ---

    @Test
    void getMyStatusForReservationsShouldReturnMap() {
        Map<Long, String> statuses = Map.of(1L, "accepted", 2L, "declined");
        when(reservationRepository.findParticipantStatusesForUser(1L, "2024-06-01", "2024-06-30"))
                .thenReturn(statuses);

        Map<Long, String> result = reservationService.getMyStatusForReservations(1L, "2024-06-01", "2024-06-30");

        assertEquals(2, result.size());
        assertEquals("accepted", result.get(1L));
    }
}
