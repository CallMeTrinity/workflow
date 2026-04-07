package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.model.Project;
import org.example.model.User;
import org.example.model.UserStory;
import org.example.model.enums.Priority;
import org.example.model.enums.Status;
import org.example.repository.ProjectRepository;
import org.example.service.TaskService;
import org.example.service.UserStoryService;
import org.example.model.Task;

import java.time.LocalDate;
import java.util.List;

public class CreateTaskController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private ComboBox<UserStory> userStoryBox;
    @FXML private ComboBox<User> assigneeBox;
    @FXML private ComboBox<User> taskLeaderBox;
    @FXML private Label errorLabel;

    private final TaskService taskService = new TaskService();
    private final UserStoryService userStoryService = new UserStoryService();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private Project project;
    private Task taskToEdit = null;

    @FXML
    public void initialize() {
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(Priority.MEDIUM);

        userStoryBox.setConverter(new StringConverter<>() {
            @Override public String toString(UserStory us) { return us == null ? "Aucune" : us.getTitle(); }
            @Override public UserStory fromString(String s) { return null; }
        });

        StringConverter<User> userConverter = new StringConverter<>() {
            @Override public String toString(User u) { return u == null ? "Aucun" : u.getFirstName() + " " + u.getLastName(); }
            @Override public User fromString(String s) { return null; }
        };
        assigneeBox.setConverter(userConverter);
        taskLeaderBox.setConverter(userConverter);
    }

    public void setProject(Project project) {
        this.project = project;
        loadUserStories();
        loadMembers();
    }

    private void loadUserStories() {
        List<UserStory> userStories = userStoryService.getUserStoriesByProject(project.getId());
        userStoryBox.getItems().clear();
        userStoryBox.getItems().add(null);
        userStoryBox.getItems().addAll(userStories);
        userStoryBox.setValue(null);
    }

    private void loadMembers() {
        List<User> members = projectRepository.findMembers(project.getId());

        assigneeBox.getItems().clear();
        assigneeBox.getItems().add(null);
        assigneeBox.getItems().addAll(members);
        assigneeBox.setValue(null);

        taskLeaderBox.getItems().clear();
        taskLeaderBox.getItems().add(null);
        taskLeaderBox.getItems().addAll(members);

        // Pre-select the project leader
        members.stream()
                .filter(u -> u.getId().equals(project.getProjectLeaderId()))
                .findFirst()
                .ifPresent(taskLeaderBox::setValue);
    }

    public void setTask(Task task) {
        this.taskToEdit = task;

        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            deadlinePicker.setValue(LocalDate.parse(task.getDeadline()));
        }
        priorityBox.setValue(task.getPriority());

        if (task.getUserStoryId() != null) {
            for (UserStory us : userStoryBox.getItems()) {
                if (us != null && us.getId().equals(task.getUserStoryId())) {
                    userStoryBox.setValue(us);
                    break;
                }
            }
        }

        if (task.getAssignedUserId() != null) {
            for (User u : assigneeBox.getItems()) {
                if (u != null && u.getId().equals(task.getAssignedUserId())) {
                    assigneeBox.setValue(u);
                    break;
                }
            }
        }

        if (task.getTaskLeaderId() != null) {
            for (User u : taskLeaderBox.getItems()) {
                if (u != null && u.getId().equals(task.getTaskLeaderId())) {
                    taskLeaderBox.setValue(u);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();

        if (title.isEmpty()) {
            errorLabel.setText("Le titre est obligatoire");
            return;
        }

        String deadline = deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : null;
        Long userStoryId = userStoryBox.getValue() != null ? userStoryBox.getValue().getId() : null;
        Long assignedId  = assigneeBox.getValue()   != null ? assigneeBox.getValue().getId()   : null;
        Long leaderId    = taskLeaderBox.getValue()  != null ? taskLeaderBox.getValue().getId()  : null;

        try {
            if (taskToEdit == null) {
                taskService.createTask(
                        title,
                        descriptionField.getText(),
                        Status.TODO,
                        priorityBox.getValue(),
                        deadline,
                        null,
                        assignedId,
                        project.getId(),
                        userStoryId,
                        leaderId
                );
            } else {
                taskToEdit.setTitle(title);
                taskToEdit.setDescription(descriptionField.getText());
                taskToEdit.setDeadline(deadline);
                taskToEdit.setPriority(priorityBox.getValue());
                taskToEdit.setUserStoryId(userStoryId);
                taskToEdit.setAssignedUserId(assignedId);
                taskToEdit.setTaskLeaderId(leaderId);

                taskService.updateTask(taskToEdit);
            }

            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
