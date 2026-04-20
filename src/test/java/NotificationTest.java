import org.example.model.Notification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest {

    @Test
    void fourParamConstructorShouldSetFieldsWithNullProjectId() {
        Notification n = new Notification(1L, "Hello", 2L, false);

        assertEquals(1L, n.getId());
        assertEquals("Hello", n.getMessage());
        assertEquals(2L, n.getUserId());
        assertFalse(n.isRead());
        assertNull(n.getProjectId());
    }

    @Test
    void fiveParamConstructorShouldSetAllFields() {
        Notification n = new Notification(1L, "Hello", 2L, true, 3L);

        assertEquals(1L, n.getId());
        assertEquals("Hello", n.getMessage());
        assertEquals(2L, n.getUserId());
        assertTrue(n.isRead());
        assertEquals(3L, n.getProjectId());
    }

    @Test
    void settersShouldUpdateFields() {
        Notification n = new Notification();
        n.setId(5L);
        n.setMessage("Updated");
        n.setUserId(10L);
        n.setIsRead(true);
        n.setProjectId(7L);

        assertEquals(5L, n.getId());
        assertEquals("Updated", n.getMessage());
        assertEquals(10L, n.getUserId());
        assertTrue(n.isRead());
        assertEquals(7L, n.getProjectId());
    }

    @Test
    void isReadToggleShouldWork() {
        Notification n = new Notification(1L, "msg", 1L, false);
        assertFalse(n.isRead());

        n.setIsRead(true);
        assertTrue(n.isRead());

        n.setIsRead(false);
        assertFalse(n.isRead());
    }
}
