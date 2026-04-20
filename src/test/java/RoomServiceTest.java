import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.Room;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.RoomRepository;
import org.example.service.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomService roomService;

    private User adminUser;
    private User memberUser;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository);
        adminUser = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN, null);
        memberUser = new User(3L, "Member", "Jane", "member@test.com", "hash", Role.MEMBER, null);
    }

    @AfterEach
    void tearDown() {
        SessionManager.clear();
    }

    @Test
    void adminShouldCreateRoom() {
        SessionManager.setCurrentUser(adminUser);
        when(roomRepository.save(any(Room.class))).thenReturn(1L);

        Room result = roomService.createRoom("Salle A", 10);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Salle A", result.getName());
        assertEquals(10, result.getCapacity());
    }

    @Test
    void nonAdminShouldNotCreateRoom() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> roomService.createRoom("Salle A", 10));
        verify(roomRepository, never()).save(any());
    }

    @Test
    void shouldReturnRoomById() {
        Room room = new Room(1L, "Salle A", 10);
        when(roomRepository.findById(1L)).thenReturn(room);

        Room result = roomService.getRoomById(1L);

        assertEquals("Salle A", result.getName());
    }

    @Test
    void shouldThrowNotFoundIfRoomMissing() {
        when(roomRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> roomService.getRoomById(99L));
    }

    @Test
    void shouldReturnAllRooms() {
        List<Room> rooms = List.of(new Room(1L, "A", 10), new Room(2L, "B", 20));
        when(roomRepository.findAll()).thenReturn(rooms);

        List<Room> result = roomService.getAllRooms();

        assertEquals(2, result.size());
    }

    @Test
    void adminShouldUpdateRoom() {
        SessionManager.setCurrentUser(adminUser);
        Room room = new Room(1L, "Updated", 15);

        roomService.updateRoom(room);

        verify(roomRepository).update(room);
    }

    @Test
    void nonAdminShouldNotUpdateRoom() {
        SessionManager.setCurrentUser(memberUser);
        Room room = new Room(1L, "Updated", 15);

        assertThrows(AutorisationException.class, () -> roomService.updateRoom(room));
        verify(roomRepository, never()).update(any());
    }

    @Test
    void adminShouldDeleteRoom() {
        SessionManager.setCurrentUser(adminUser);

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(1L);
    }

    @Test
    void nonAdminShouldNotDeleteRoom() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> roomService.deleteRoom(1L));
        verify(roomRepository, never()).delete(any());
    }

    @Test
    void projectLeaderShouldNotCreateRoom() {
        User leaderUser = new User(2L, "Leader", "John", "leader@test.com", "hash", Role.PROJECT_LEADER, null);
        SessionManager.setCurrentUser(leaderUser);

        assertThrows(AutorisationException.class, () -> roomService.createRoom("Salle B", 5));
    }
}
