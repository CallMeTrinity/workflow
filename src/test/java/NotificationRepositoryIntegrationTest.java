import org.example.model.Notification;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRepositoryIntegrationTest extends BaseIntegrationTest {

    private NotificationRepository notificationRepository;
    private Long userId;

    @BeforeEach
    void setUp() {
        notificationRepository = new NotificationRepository();

        UserRepository userRepo = new UserRepository();
        userId = userRepo.save(new User(null, "Notif", "User",
                "notif@test.com", "hash", Role.MEMBER, null));
    }

    @Test
    void shouldSaveAndFindByUser() {
        Notification notif = new Notification(null, "Nouvelle tache", userId, false);
        notificationRepository.save(notif);

        List<Notification> found = notificationRepository.findByUser(userId);

        assertEquals(1, found.size());
        assertEquals("Nouvelle tache", found.get(0).getMessage());
        assertEquals(userId, found.get(0).getUserId());
        assertFalse(found.get(0).isRead());
    }

    @Test
    void shouldSaveWithProjectId() {
        // Creer un vrai projet pour la FK
        org.example.repository.ProjectRepository projectRepo =
                new org.example.repository.ProjectRepository();
        Long projectId = projectRepo.save(new org.example.model.Project(
                null, "P", "D", "2024-01-01", "2024-12-31", userId));

        Notification notif = new Notification(null, "Project notif", userId, false, projectId);
        notificationRepository.save(notif);

        List<Notification> found = notificationRepository.findByUser(userId);
        assertEquals(1, found.size());
        assertEquals(projectId, found.get(0).getProjectId());
    }

    @Test
    void shouldSaveWithNullProjectId() {
        Notification notif = new Notification(null, "No project", userId, false, null);
        notificationRepository.save(notif);

        List<Notification> found = notificationRepository.findByUser(userId);
        assertEquals(1, found.size());
        assertNull(found.get(0).getProjectId());
    }

    @Test
    void shouldFindById() {
        notificationRepository.save(new Notification(null, "Find me", userId, false));

        List<Notification> all = notificationRepository.findByUser(userId);
        Long id = all.get(0).getId();

        Notification found = notificationRepository.findById(id);
        assertNotNull(found);
        assertEquals("Find me", found.getMessage());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(notificationRepository.findById(999L));
    }

    @Test
    void shouldCountUnread() {
        notificationRepository.save(new Notification(null, "Unread 1", userId, false));
        notificationRepository.save(new Notification(null, "Unread 2", userId, false));

        int count = notificationRepository.countUnread(userId);

        assertEquals(2, count);
    }

    @Test
    void shouldReturnZeroUnreadWhenNone() {
        assertEquals(0, notificationRepository.countUnread(userId));
    }

    @Test
    void shouldUpdateNotification() {
        notificationRepository.save(new Notification(null, "Original", userId, false));
        List<Notification> all = notificationRepository.findByUser(userId);
        Notification notif = all.get(0);

        notif.setMessage("Updated");
        notif.setIsRead(true);
        notificationRepository.update(notif);

        Notification found = notificationRepository.findById(notif.getId());
        assertEquals("Updated", found.getMessage());
        assertTrue(found.isRead());
    }

    @Test
    void shouldUpdateWithProjectId() {
        notificationRepository.save(new Notification(null, "Msg", userId, false, null));
        List<Notification> all = notificationRepository.findByUser(userId);
        Notification notif = all.get(0);

        notif.setProjectId(null);
        notificationRepository.update(notif);

        Notification found = notificationRepository.findById(notif.getId());
        assertNull(found.getProjectId());
    }

    @Test
    void shouldMarkAllAsRead() {
        notificationRepository.save(new Notification(null, "N1", userId, false));
        notificationRepository.save(new Notification(null, "N2", userId, false));

        notificationRepository.markAllAsRead(userId);

        int unread = notificationRepository.countUnread(userId);
        assertEquals(0, unread);

        List<Notification> all = notificationRepository.findByUser(userId);
        assertTrue(all.stream().allMatch(Notification::isRead));
    }

    @Test
    void shouldDeleteNotification() {
        notificationRepository.save(new Notification(null, "ToDelete", userId, false));
        List<Notification> all = notificationRepository.findByUser(userId);
        Long id = all.get(0).getId();

        notificationRepository.delete(id);

        assertNull(notificationRepository.findById(id));
    }

    @Test
    void shouldOrderByIdDesc() {
        notificationRepository.save(new Notification(null, "First", userId, false));
        notificationRepository.save(new Notification(null, "Second", userId, false));

        List<Notification> all = notificationRepository.findByUser(userId);

        assertEquals(2, all.size());
        assertEquals("Second", all.get(0).getMessage());
        assertEquals("First", all.get(1).getMessage());
    }
}
