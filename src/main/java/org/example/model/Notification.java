package org.example.model;

public class Notification {

    private Long id;
    private String message;
    private Long userId;
    private boolean isRead;
    private Long projectId;

    public Notification() {}

    public Notification(Long id, String message, Long userId, boolean isRead) {
        this(id, message, userId, isRead, null);
    }

    public Notification(Long id, String message, Long userId, boolean isRead, Long projectId) {
        this.id = id;
        this.message = message;
        this.userId = userId;
        this.isRead = isRead;
        this.projectId = projectId;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public Long getUserId() { return userId; }
    public boolean isRead() { return isRead; }
    public Long getProjectId() { return projectId; }

    public void setId(Long id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
