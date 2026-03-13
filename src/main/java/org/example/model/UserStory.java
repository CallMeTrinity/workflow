package org.example.model;

import org.example.model.enums.Priority;

public class UserStory {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Long projectId;

    public UserStory (){
    }

    public UserStory(Long id, String title, String description, Priority priority, Long projectId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.projectId = projectId;
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
