package org.example.ui.controller;

import org.example.config.SessionManager;
import org.example.model.User;
import org.example.model.enums.Role;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.example.model.Project;
import org.example.service.ProjectService;

import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private ListView<String> projectList;

    private ProjectService projectService = new ProjectService();

    private List<Project> projects;

    @FXML
    public void initialize() {

        SessionManager.setCurrentUser(
                new User(1L, "Test", "User", "test@test.com", "pass", Role.ADMIN)
        );

        System.out.println("User connecté : " + SessionManager.getCurrentUser());

        projects = projectService.getAllProjects();

        for (Project project : projects) {
            projectList.getItems().add(project.getName());
        }

        refreshProjects();
        }

    private void refreshProjects() {

        projectList.getItems().clear();

        projects = projectService.getAllProjects();

        for (Project project : projects) {
            projectList.getItems().add(project.getName());
        }
    }

    @FXML
    private void openCreateProject() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createProject.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Créer un projet");
            stage.setScene(scene);
            stage.showAndWait(); // IMPORTANT

            refreshProjects();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}