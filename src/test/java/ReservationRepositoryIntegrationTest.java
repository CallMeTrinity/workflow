import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ReservationRepository;
import org.example.repository.RoomRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReservationRepositoryIntegrationTest extends BaseIntegrationTest {

    private ReservationRepository reservationRepository;
    private Long roomId;
    private Long organizerId;
    private Long participantId;

    @BeforeEach
    void setUp() {
        reservationRepository = new ReservationRepository();

        UserRepository userRepo = new UserRepository();
        organizerId = userRepo.save(new User(null, "Organizer", "Test",
                "organizer@test.com", "hash", Role.MEMBER, null));
        participantId = userRepo.save(new User(null, "Participant", "Test",
                "participant@test.com", "hash", Role.MEMBER, null));

        RoomRepository roomRepo = new RoomRepository();
        roomId = roomRepo.save(new Room(null, "Salle Test", 10));
    }

    @Test
    void shouldSaveAndFindById() {
        Reservation r = new Reservation(null, "Reunion", "Desc", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId);

        Long id = reservationRepository.save(r);

        assertNotNull(id);
        Reservation found = reservationRepository.findById(id);
        assertNotNull(found);
        assertEquals("Reunion", found.getTitle());
        assertEquals("Desc", found.getDescription());
        assertEquals("2024-06-15", found.getDate());
        assertEquals("09:00", found.getStartTime());
        assertEquals("10:00", found.getEndTime());
        assertEquals(roomId, found.getRoomId());
        assertNull(found.getProjectId());
        assertEquals(organizerId, found.getOrganizerId());
    }

    @Test
    void shouldSaveWithProjectId() {
        // projectId n'a pas de FK strict ici, on teste le nullable
        Reservation r = new Reservation(null, "R", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId);
        Long id = reservationRepository.save(r);

        Reservation found = reservationRepository.findById(id);
        assertNull(found.getProjectId());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(reservationRepository.findById(999L));
    }

    @Test
    void shouldFindAll() {
        reservationRepository.save(new Reservation(null, "R1", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.save(new Reservation(null, "R2", "D", "2024-06-16",
                "14:00", "15:00", roomId, null, organizerId));

        List<Reservation> all = reservationRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldFindByRoomAndDate() {
        reservationRepository.save(new Reservation(null, "Room1", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.save(new Reservation(null, "Room2", "D", "2024-06-16",
                "09:00", "10:00", roomId, null, organizerId));

        List<Reservation> found = reservationRepository.findByRoomAndDate(roomId, "2024-06-15");

        assertEquals(1, found.size());
        assertEquals("Room1", found.get(0).getTitle());
    }

    @Test
    void shouldFindByRoomAndDateReturnEmptyWhenNone() {
        List<Reservation> found = reservationRepository.findByRoomAndDate(roomId, "2099-01-01");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindByOrganizer() {
        reservationRepository.save(new Reservation(null, "ByOrg", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));

        List<Reservation> found = reservationRepository.findByOrganizer(organizerId);

        assertFalse(found.isEmpty());
        assertEquals("ByOrg", found.get(0).getTitle());
    }

    @Test
    void shouldUpdateReservation() {
        Long id = reservationRepository.save(new Reservation(null, "Old", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));

        reservationRepository.update(new Reservation(id, "Updated", "New D", "2024-06-20",
                "14:00", "16:00", roomId, null, organizerId));

        Reservation found = reservationRepository.findById(id);
        assertEquals("Updated", found.getTitle());
        assertEquals("New D", found.getDescription());
        assertEquals("2024-06-20", found.getDate());
        assertEquals("14:00", found.getStartTime());
        assertEquals("16:00", found.getEndTime());
    }

    @Test
    void shouldDeleteReservation() {
        Long id = reservationRepository.save(new Reservation(null, "Del", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));

        reservationRepository.delete(id);

        assertNull(reservationRepository.findById(id));
    }

    // --- Participants ---

    @Test
    void shouldAddAndFindParticipantIds() {
        Long resId = reservationRepository.save(new Reservation(null, "R", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));

        reservationRepository.addParticipant(resId, participantId);

        List<Long> ids = reservationRepository.findParticipantIds(resId);
        assertEquals(1, ids.size());
        assertTrue(ids.contains(participantId));
    }

    @Test
    void shouldRemoveParticipant() {
        Long resId = reservationRepository.save(new Reservation(null, "R", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.addParticipant(resId, participantId);

        reservationRepository.removeParticipant(resId, participantId);

        List<Long> ids = reservationRepository.findParticipantIds(resId);
        assertTrue(ids.isEmpty());
    }

    @Test
    void shouldFindByParticipantAndDate() {
        Long resId = reservationRepository.save(new Reservation(null, "ParticipantRes", "D",
                "2024-06-15", "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.addParticipant(resId, participantId);

        List<Reservation> found = reservationRepository.findByParticipantAndDate(
                participantId, "2024-06-15");

        assertEquals(1, found.size());
        assertEquals("ParticipantRes", found.get(0).getTitle());
    }

    @Test
    void shouldFindByParticipantAndDateReturnEmptyWhenNone() {
        List<Reservation> found = reservationRepository.findByParticipantAndDate(
                participantId, "2024-06-15");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldUpdateParticipantStatus() {
        Long resId = reservationRepository.save(new Reservation(null, "R", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.addParticipant(resId, participantId);

        reservationRepository.updateParticipantStatus(resId, participantId, "accepted");

        Map<Long, String> statuses = reservationRepository.findParticipantStatusesForUser(
                participantId, "2024-06-01", "2024-06-30");
        assertEquals("accepted", statuses.get(resId));
    }

    @Test
    void shouldFindParticipantStatusesForUser() {
        Long res1 = reservationRepository.save(new Reservation(null, "R1", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));
        Long res2 = reservationRepository.save(new Reservation(null, "R2", "D", "2024-06-20",
                "14:00", "15:00", roomId, null, organizerId));

        reservationRepository.addParticipant(res1, participantId);
        reservationRepository.addParticipant(res2, participantId);
        reservationRepository.updateParticipantStatus(res1, participantId, "accepted");
        reservationRepository.updateParticipantStatus(res2, participantId, "declined");

        Map<Long, String> statuses = reservationRepository.findParticipantStatusesForUser(
                participantId, "2024-06-01", "2024-06-30");

        assertEquals(2, statuses.size());
        assertEquals("accepted", statuses.get(res1));
        assertEquals("declined", statuses.get(res2));
    }

    @Test
    void shouldFindForUserInRangeAsOrganizer() {
        reservationRepository.save(new Reservation(null, "OrgRes", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));

        List<Reservation> found = reservationRepository.findForUserInRange(
                organizerId, "2024-06-01", "2024-06-30");

        assertFalse(found.isEmpty());
        assertEquals("OrgRes", found.get(0).getTitle());
    }

    @Test
    void shouldFindForUserInRangeAsParticipant() {
        Long resId = reservationRepository.save(new Reservation(null, "PartRes", "D",
                "2024-06-15", "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.addParticipant(resId, participantId);

        List<Reservation> found = reservationRepository.findForUserInRange(
                participantId, "2024-06-01", "2024-06-30");

        assertFalse(found.isEmpty());
        assertEquals("PartRes", found.get(0).getTitle());
    }

    @Test
    void shouldFindForUserInRangeRespectDateBounds() {
        reservationRepository.save(new Reservation(null, "InRange", "D", "2024-06-15",
                "09:00", "10:00", roomId, null, organizerId));
        reservationRepository.save(new Reservation(null, "OutOfRange", "D", "2024-07-15",
                "09:00", "10:00", roomId, null, organizerId));

        List<Reservation> found = reservationRepository.findForUserInRange(
                organizerId, "2024-06-01", "2024-06-30");

        assertEquals(1, found.size());
        assertEquals("InRange", found.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyParticipantIdsForUnknownReservation() {
        List<Long> ids = reservationRepository.findParticipantIds(999L);
        assertTrue(ids.isEmpty());
    }
}
