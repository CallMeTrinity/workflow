package org.example.model;

public class Notification {

    private Long id;
    private String message;
    private Long userId;
    private boolean isRead;

    public Notification() {
    }

    public Notification(Long id, String message, Long userId, boolean isRead) {
        this.id = id;
        this.message = message;
        this.userId = userId;
        this.isRead = isRead;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isRead() { return isRead; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
}