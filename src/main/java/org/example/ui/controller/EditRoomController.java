package org.example.ui.controller;

import com.sun.javafx.scene.control.IntegerField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.Room;
import org.example.service.RoomService;


public class EditRoomController {

    @FXML private TextField nameField;
    @FXML private TextField capacityField;
    @FXML private Label errorLabel;

    private final RoomService roomService = new RoomService();
    private Room room;

    public void setRoom(Room room) {
        this.room = room;
        nameField.setText(room.getName());
        capacityField.setText(String.valueOf(room.getCapacity()));
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Le nom est obligatoire");
            return;
        }

        try {
            room.setName(name);
            room.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            roomService.updateRoom(room);
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
