package org.example.model;

import org.example.model.enums.Priority;
import org.example.model.enums.Status;

// Représente une tâche du projet

public class Task {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private String deadline;    // format YYYY-MM-DD
    private Integer timeEstimate; // en heures
    private Long projectId;
    private Long userStoryId; // peut être null
    private Long assignedUserId; // peut être null



    public Task() {
    }

    public Task(Long id, String title, String description, Status status, Priority priority, String deadline, Integer timeEstimate, Long projectId, Long userStoryId, Long assignedUserId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.timeEstimate = timeEstimate;
        this.assignedUserId = assignedUserId;
        this.projectId = projectId;
        this.userStoryId = userStoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(Integer timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserStoryId() {
        return userStoryId;
    }

    public void setUserStoryId(Long userStoryId) {
        this.userStoryId = userStoryId;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
}
