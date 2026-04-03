import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.exception.ReservationConflictException;
import org.example.model.Reservation;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User adminUser;
    private User memberUser;
    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        adminUser  = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN, null);
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
}
