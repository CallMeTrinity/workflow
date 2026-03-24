package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import org.example.model.Project;
import org.example.model.Task;
import org.example.model.enums.Status;
import org.example.model.enums.Priority;
import org.example.service.TaskService;
import org.example.config.SessionManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class KanbanController {

    @FXML
    private ListView<String> todoList;

    @FXML
    private ListView<String> inProgressList;

    @FXML
    private ListView<String> doneList;

    private Project project;

    private TaskService taskService = new TaskService();

    @FXML
    public void initialize() {
        // Rien ici → on attend setProject()
    }

    /**
     * Reçoit le projet depuis le Dashboard
     */
    public void setProject(Project project) {
        this.project = project;
        loadTasks();
    }

    /**
     * Charge les tâches du projet et les répartit dans les colonnes
     */
    private void loadTasks() {

        if (project == null) return;

        List<Task> tasks = taskService.getTasksByProject(project.getId());

        // Reset des colonnes
        todoList.getItems().clear();
        inProgressList.getItems().clear();
        doneList.getItems().clear();

        // Répartition
        for (Task task : tasks) {

            switch (task.getStatus()) {

                case TODO:
                    todoList.getItems().add(task.getTitle());
                    break;

                case IN_PROGRESS:
                    inProgressList.getItems().add(task.getTitle());
                    break;

                case DONE:
                    doneList.getItems().add(task.getTitle());
                    break;
            }
        }
    }

    /**
     * Ajout rapide d'une tâche (test)
     */
    @FXML
    private void handleAddTask() {

        taskService.createTask(
                "Nouvelle tâche",
                "Description test",
                Status.TODO,
                Priority.MEDIUM,
                "2026-12-31",
                2,
                SessionManager.getCurrentUser().getId(),
                project.getId(),
                null
        );

        loadTasks();
    }

    /**
     * Changer statut → TODO → IN_PROGRESS
     */
    @FXML
    private void handleTodoClick() {

        String selected = todoList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Task task = findTaskByTitle(selected);

        if (task != null) {
            taskService.updateTaskStatus(task.getId(), Status.IN_PROGRESS);
            loadTasks();
        }
    }

    /**
     * IN_PROGRESS → DONE
     */
    @FXML
    private void handleInProgressClick() {

        String selected = inProgressList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Task task = findTaskByTitle(selected);

        if (task != null) {
            taskService.updateTaskStatus(task.getId(), Status.DONE);
            loadTasks();
        }
    }

    /**
     * DONE → TODO
     */
    @FXML
    private void handleDoneClick() {

        String selected = doneList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Task task = findTaskByTitle(selected);

        if (task != null) {
            taskService.updateTaskStatus(task.getId(), Status.TODO);
            loadTasks();
        }
    }

    /**
     * Permet de retrouver une Task via son titre
     */
    private Task findTaskByTitle(String title) {

        List<Task> tasks = taskService.getTasksByProject(project.getId());

        for (Task task : tasks) {
            if (task.getTitle().equals(title)) {
                return task;
            }
        }

        return null;
    }
    @FXML
    private void handleBack() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) todoList.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}