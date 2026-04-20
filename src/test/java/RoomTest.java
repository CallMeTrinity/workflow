import org.example.model.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {

    @Test
    void fullConstructorShouldSetFields() {
        Room room = new Room(1L, "Salle A", 10);

        assertEquals(1L, room.getId());
        assertEquals("Salle A", room.getName());
        assertEquals(10, room.getCapacity());
    }

    @Test
    void defaultConstructorShouldCreateEmptyRoom() {
        Room room = new Room();

        assertNull(room.getId());
        assertNull(room.getName());
        assertEquals(0, room.getCapacity());
    }

    @Test
    void settersShouldUpdateFields() {
        Room room = new Room();
        room.setId(5L);
        room.setName("Salle B");
        room.setCapacity(20);

        assertEquals(5L, room.getId());
        assertEquals("Salle B", room.getName());
        assertEquals(20, room.getCapacity());
    }
}
