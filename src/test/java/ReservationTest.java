import org.example.model.Reservation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationTest {

    @Test
    void fullConstructorShouldSetFields() {
        Reservation r = new Reservation(1L, "Meeting", "Desc", "2024-06-01",
                "09:00", "10:00", 2L, 3L, 4L);

        assertEquals(1L, r.getId());
        assertEquals("Meeting", r.getTitle());
        assertEquals("Desc", r.getDescription());
        assertEquals("2024-06-01", r.getDate());
        assertEquals("09:00", r.getStartTime());
        assertEquals("10:00", r.getEndTime());
        assertEquals(2L, r.getRoomId());
        assertEquals(3L, r.getProjectId());
        assertEquals(4L, r.getOrganizerId());
    }

    @Test
    void settersShouldUpdateFields() {
        Reservation r = new Reservation();
        r.setId(10L);
        r.setTitle("Updated");
        r.setDescription("New desc");
        r.setDate("2024-07-01");
        r.setStartTime("14:00");
        r.setEndTime("15:00");
        r.setRoomId(5L);
        r.setProjectId(6L);
        r.setOrganizerId(7L);

        assertEquals(10L, r.getId());
        assertEquals("Updated", r.getTitle());
        assertEquals("New desc", r.getDescription());
        assertEquals("2024-07-01", r.getDate());
        assertEquals("14:00", r.getStartTime());
        assertEquals("15:00", r.getEndTime());
        assertEquals(5L, r.getRoomId());
        assertEquals(6L, r.getProjectId());
        assertEquals(7L, r.getOrganizerId());
    }

    @Test
    void projectIdShouldBeNullable() {
        Reservation r = new Reservation(1L, "Meeting", "Desc", "2024-06-01",
                "09:00", "10:00", 2L, null, 4L);

        assertNull(r.getProjectId());
    }
}
