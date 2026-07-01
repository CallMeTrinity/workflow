package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.service.RoomService;
import org.example.ui.util.Modals;

public class CreateRoomController {

    @FXML private TextField nameField;
    @FXML private TextField capacityField;
    @FXML private Label errorLabel;

    private final RoomService roomService = new RoomService();

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();
        String capacityStr = capacityField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("Le nom est obligatoire");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("La capacité doit être un nombre supérieur à 0");
            return;
        }

        try {
            roomService.createRoom(name, capacity);
            Modals.close(nameField);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
