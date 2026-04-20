import org.example.model.Notification;
import org.example.repository.NotificationRepository;
import org.example.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void createNotificationWithoutProjectId() {
        notificationService.createNotification(1L, "Hello");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("Hello", saved.getMessage());
        assertNull(saved.getProjectId());
    }

    @Test
    void createNotificationWithProjectId() {
        notificationService.createNotification(1L, "Task assigned", 42L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("Task assigned", saved.getMessage());
        assertEquals(42L, saved.getProjectId());
    }

    @Test
    void getUserNotificationsShouldReturnList() {
        List<Notification> expected = List.of(
                new Notification(1L, "msg1", 1L, false),
                new Notification(2L, "msg2", 1L, true)
        );
        when(notificationRepository.findByUser(1L)).thenReturn(expected);

        List<Notification> result = notificationService.getUserNotifications(1L);

        assertEquals(2, result.size());
        assertEquals("msg1", result.get(0).getMessage());
    }

    @Test
    void getUnreadCountShouldReturnRepositoryCount() {
        when(notificationRepository.countUnread(1L)).thenReturn(5);

        int count = notificationService.getUnreadCount(1L);

        assertEquals(5, count);
    }

    @Test
    void markAllAsReadShouldCallRepository() {
        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsRead(1L);
    }

    @Test
    void toggleReadShouldFlipFalseToTrue() {
        Notification notif = new Notification(1L, "msg", 1L, false);
        when(notificationRepository.findById(1L)).thenReturn(notif);

        notificationService.toggleRead(1L);

        verify(notificationRepository).update(notif);
        assertTrue(notif.isRead());
    }

    @Test
    void toggleReadShouldFlipTrueToFalse() {
        Notification notif = new Notification(1L, "msg", 1L, true);
        when(notificationRepository.findById(1L)).thenReturn(notif);

        notificationService.toggleRead(1L);

        verify(notificationRepository).update(notif);
        assertFalse(notif.isRead());
    }

    @Test
    void toggleReadWithNonExistentNotificationShouldDoNothing() {
        when(notificationRepository.findById(99L)).thenReturn(null);

        notificationService.toggleRead(99L);

        verify(notificationRepository, never()).update(any());
    }

    @Test
    void deleteNotificationShouldCallRepository() {
        notificationService.deleteNotification(1L);

        verify(notificationRepository).delete(1L);
    }
}
