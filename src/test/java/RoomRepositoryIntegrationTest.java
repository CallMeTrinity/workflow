import org.example.model.Room;
import org.example.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomRepositoryIntegrationTest extends BaseIntegrationTest {

    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        roomRepository = new RoomRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Room room = new Room(null, "Salle A", 10);
        Long id = roomRepository.save(room);

        assertNotNull(id);
        Room found = roomRepository.findById(id);
        assertNotNull(found);
        assertEquals("Salle A", found.getName());
        assertEquals(10, found.getCapacity());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(roomRepository.findById(999L));
    }

    @Test
    void shouldFindAll() {
        roomRepository.save(new Room(null, "Room1", 5));
        roomRepository.save(new Room(null, "Room2", 20));

        List<Room> all = roomRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldUpdateRoom() {
        Long id = roomRepository.save(new Room(null, "Old Room", 5));

        roomRepository.update(new Room(id, "New Room", 15));

        Room found = roomRepository.findById(id);
        assertEquals("New Room", found.getName());
        assertEquals(15, found.getCapacity());
    }

    @Test
    void shouldDeleteRoom() {
        Long id = roomRepository.save(new Room(null, "ToDelete", 5));

        roomRepository.delete(id);

        assertNull(roomRepository.findById(id));
    }
}
