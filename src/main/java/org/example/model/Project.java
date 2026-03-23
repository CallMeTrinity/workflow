package org.example.model;

/**
 * Représente un projet dans l'application.
 */
public class Project {

    private Long id;
    private String name;
    private String description;
    private String startDate;  // format YYYY-MM-DD
    private String endDate;    // format YYYY-MM-DD
    private Long projectLeaderId;

    public Project() {
    }

    /**
     * Constructeur principal.
     * @param id l'identifiant du projet
     * @param name le nom du projet
     * @param description la description du projet
     * @param startDate la date de début (YYYY-MM-DD)
     * @param endDate la date de fin (YYYY-MM-DD)
     * @param projectLeaderId l'identifiant du chef de projet
     */
    public Project(Long id, String name, String description, String startDate, String endDate, Long projectLeaderId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectLeaderId = projectLeaderId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Long getProjectLeaderId() { return projectLeaderId; }
    public void setProjectLeaderId(Long projectLeaderId) { this.projectLeaderId = projectLeaderId; }
}
