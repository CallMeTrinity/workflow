import org.example.model.Reservation;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FindAvailableSlotsTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private static final String DATE = "2024-06-01";
    private static final Long ROOM_ID = 1L;
    private static final Long USER_1 = 10L;
    private static final Long USER_2 = 11L;

    private Reservation reservation(String start, String end) {
        return new Reservation(null, "Test", null, DATE, start, end, ROOM_ID, null, 1L);
    }

    // --- Aucun créneau occupé ---

    @Test
    void shouldReturnFullDayWhenNothingIsBooked() {
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE)).thenReturn(List.of());
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE)).thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 60, ROOM_ID
        );

        // Journée entière 08:00-20:00 = 720 min → un seul slot
        assertEquals(1, slots.size());
        assertEquals(480, slots.get(0)[0]); // 08:00
        assertEquals(1200, slots.get(0)[1]); // 20:00
    }

    // --- Filtrage par durée ---

    @Test
    void shouldFilterSlotsShorterThanDuration() {
        // Occupé 09:00-09:30 → laisse 08:00-09:00 (60min) et 09:30-20:00
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE))
                .thenReturn(List.of(reservation("09:00", "09:30")));
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE)).thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 60, ROOM_ID
        );

        // 08:00-09:00 = 60min → ok
        // 09:30-20:00 = 630min → ok
        assertEquals(2, slots.size());
    }

    @Test
    void shouldReturnEmptyWhenAllSlotsToShort() {
        // Occupé 08:30-19:30 → laisse 08:00-08:30 (30min) et 19:30-20:00 (30min)
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE))
                .thenReturn(List.of(reservation("08:30", "19:30")));
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE)).thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 60, ROOM_ID
        );

        assertEquals(0, slots.size());
    }

    // --- Fusion des intervalles ---

    @Test
    void shouldMergeOverlappingIntervals() {
        // Deux réservations qui se chevauchent → fusionnées en une
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE))
                .thenReturn(List.of(
                        reservation("09:00", "10:30"),
                        reservation("10:00", "11:00")
                ));
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE)).thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 30, ROOM_ID
        );

        // Fusionné : 09:00-11:00 → libre : 08:00-09:00 et 11:00-20:00
        assertEquals(2, slots.size());
        assertEquals(480, slots.get(0)[0]);  // 08:00
        assertEquals(540, slots.get(0)[1]);  // 09:00
        assertEquals(660, slots.get(1)[0]);  // 11:00
        assertEquals(1200, slots.get(1)[1]); // 20:00
    }

    @Test
    void shouldMergeAdjacentIntervals() {
        // 09:00-10:00 et 10:00-11:00 → fusionnés en 09:00-11:00
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE))
                .thenReturn(List.of(
                        reservation("09:00", "10:00"),
                        reservation("10:00", "11:00")
                ));
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE)).thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 30, ROOM_ID
        );

        assertEquals(2, slots.size());
        assertEquals(480, slots.get(0)[0]);  // 08:00
        assertEquals(540, slots.get(0)[1]);  // 09:00
        assertEquals(660, slots.get(1)[0]);  // 11:00
        assertEquals(1200, slots.get(1)[1]); // 20:00
    }

    // --- Plusieurs participants ---

    @Test
    void shouldCombineOccupiedSlotsFromMultipleUsers() {
        // User1 occupé 09:00-10:00, User2 occupé 14:00-15:00
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE))
                .thenReturn(List.of(reservation("09:00", "10:00")));
        when(reservationRepository.findByParticipantAndDate(USER_2, DATE))
                .thenReturn(List.of(reservation("14:00", "15:00")));
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE)).thenReturn(List.of());

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1, USER_2), DATE, 60, ROOM_ID
        );

        // Libres : 08:00-09:00, 10:00-14:00, 15:00-20:00
        assertEquals(3, slots.size());
        assertEquals(480, slots.get(0)[0]);  // 08:00
        assertEquals(540, slots.get(0)[1]);  // 09:00
        assertEquals(600, slots.get(1)[0]);  // 10:00
        assertEquals(840, slots.get(1)[1]);  // 14:00
        assertEquals(900, slots.get(2)[0]);  // 15:00
        assertEquals(1200, slots.get(2)[1]); // 20:00
    }

    // --- Contrainte salle ---

    @Test
    void shouldCrossWithRoomAvailability() {
        // User libre toute la journée mais salle occupée 10:00-12:00
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE)).thenReturn(List.of());
        when(reservationRepository.findByRoomAndDate(ROOM_ID, DATE))
                .thenReturn(List.of(reservation("10:00", "12:00")));

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 60, ROOM_ID
        );

        // Libres : 08:00-10:00, 12:00-20:00
        assertEquals(2, slots.size());
        assertEquals(480, slots.get(0)[0]);  // 08:00
        assertEquals(600, slots.get(0)[1]);  // 10:00
        assertEquals(720, slots.get(1)[0]);  // 12:00
        assertEquals(1200, slots.get(1)[1]); // 20:00
    }

    @Test
    void shouldWorkWithoutRoom() {
        // roomId = null → pas de contrainte salle
        when(reservationRepository.findByParticipantAndDate(USER_1, DATE))
                .thenReturn(List.of(reservation("10:00", "11:00")));

        List<int[]> slots = reservationService.findAvailableSlots(
                List.of(USER_1), DATE, 30, null
        );

        assertEquals(2, slots.size());
        assertEquals(480, slots.get(0)[0]);  // 08:00
        assertEquals(600, slots.get(0)[1]);  // 10:00
        assertEquals(660, slots.get(1)[0]);  // 11:00
        assertEquals(1200, slots.get(1)[1]); // 20:00
    }
}
