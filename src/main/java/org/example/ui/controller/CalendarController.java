package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.RoomService;
import org.example.service.UserService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CalendarController {

    @FXML private GridPane calendarGrid;
    @FXML private Label weekLabel;
    @FXML private ScrollPane scrollPane;

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();
    private final UserService userService = new UserService();

    private Map<Long, Room> roomById;
    private Map<Long, User> userById;
    private LocalDate currentWeekStart;
    private ContextMenu activeContextMenu;

    private static final int DAY_START  = 8 * 60;   // 8h = 480 min
    private static final int DAY_END    = 20 * 60;  // 20h = 1200 min
    private static final int SLOT_MIN   = 30;
    private static final int NUM_SLOTS  = (DAY_END - DAY_START) / SLOT_MIN; // 24 slots
    private static final double ROW_H   = 44;
    private static final double TIME_W  = 56;

    @FXML
    public void initialize() {
        roomById = roomService.getAllRooms().stream()
                .collect(Collectors.toMap(Room::getId, Function.identity()));
        userById = userService.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Force equal day column widths regardless of content
        calendarGrid.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (calendarGrid.getColumnConstraints().size() >= 8) {
                double dayW = (newVal.doubleValue() - TIME_W) / 7.0;
                for (int i = 1; i <= 7; i++) {
                    ColumnConstraints cc = calendarGrid.getColumnConstraints().get(i);
                    cc.setPrefWidth(dayW);
                    cc.setMaxWidth(dayW);
                }
            }
        });

        currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        showWeek(currentWeekStart);
    }

    @FXML private void prevWeek()  { showWeek(currentWeekStart.minusWeeks(1)); }
    @FXML private void nextWeek()  { showWeek(currentWeekStart.plusWeeks(1));  }
    @FXML private void goToToday() { showWeek(LocalDate.now().with(DayOfWeek.MONDAY)); }

    private void showWeek(LocalDate weekStart) {
        currentWeekStart = weekStart;
        LocalDate weekEnd = weekStart.plusDays(6);
        weekLabel.setText(formatWeekRange(weekStart, weekEnd));

        Long userId = SessionManager.getCurrentUser().getId();
        List<Reservation> reservations = reservationService
                .getReservationsForUser(userId, weekStart.toString(), weekEnd.toString());
        Map<Long, String> myStatuses = reservationService
                .getMyStatusForReservations(userId, weekStart.toString(), weekEnd.toString());

        buildGrid(weekStart, reservations, myStatuses);
        scrollPane.setVvalue(0);
    }

    /* ------------------------------------------------------------------ */
    /*  Grid construction                                                   */
    /* ------------------------------------------------------------------ */

    private void buildGrid(LocalDate weekStart, List<Reservation> reservations,
                           Map<Long, String> myStatuses) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Column 0: time (fixed width)
        ColumnConstraints timeCol = new ColumnConstraints(TIME_W, TIME_W, TIME_W);
        calendarGrid.getColumnConstraints().add(timeCol);

        // Columns 1-7: days (equal width)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setHgrow(Priority.ALWAYS);
            dayCol.setMinWidth(80);
            dayCol.setFillWidth(true);
            calendarGrid.getColumnConstraints().add(dayCol);
        }

        // Row 0: headers (fixed height)
        calendarGrid.getRowConstraints().add(new RowConstraints(54));

        // Rows 1-NUM_SLOTS: time slots (fixed height)
        for (int i = 0; i < NUM_SLOTS; i++) {
            RowConstraints rc = new RowConstraints(ROW_H, ROW_H, ROW_H);
            calendarGrid.getRowConstraints().add(rc);
        }

        LocalDate today = LocalDate.now();

        // Corner cell (0, 0)
        Region corner = new Region();
        corner.getStyleClass().add("cal-corner");
        corner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        calendarGrid.add(corner, 0, 0);

        // Day headers (row 0, cols 1-7)
        String[] dayNames = {"Lun.", "Mar.", "Mer.", "Jeu.", "Ven.", "Sam.", "Dim."};
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);
        for (int d = 0; d < 7; d++) {
            LocalDate day = weekStart.plusDays(d);
            boolean isToday = day.equals(today);

            VBox header = new VBox(2);
            header.setAlignment(Pos.CENTER);
            header.getStyleClass().add("cal-day-header");
            if (isToday) header.getStyleClass().add("cal-day-today");

            Label nameLabel = new Label(dayNames[d]);
            nameLabel.getStyleClass().add("cal-day-name");
            Label dateLabel = new Label(day.format(dtf));
            dateLabel.getStyleClass().add(isToday ? "cal-day-date-today" : "cal-day-date");

            header.getChildren().addAll(nameLabel, dateLabel);
            header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            calendarGrid.add(header, d + 1, 0);
        }

        // Time labels and slot background cells
        for (int slot = 0; slot < NUM_SLOTS; slot++) {
            int minutes = DAY_START + slot * SLOT_MIN;
            boolean isHour = (minutes % 60 == 0);

            Region timeBg = new Region();
            timeBg.getStyleClass().add("cal-time-bg");
            timeBg.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            calendarGrid.add(timeBg, 0, slot + 1);

            if (isHour) {
                Label tl = new Label(String.format("%02d:00", minutes / 60));
                tl.getStyleClass().add("cal-time-label");
                GridPane.setValignment(tl, VPos.TOP);
                GridPane.setHalignment(tl, HPos.RIGHT);
                GridPane.setMargin(tl, new Insets(3, 8, 0, 0));
                calendarGrid.add(tl, 0, slot + 1);
            }

            for (int d = 0; d < 7; d++) {
                Region cell = new Region();
                cell.getStyleClass().add("cal-slot");
                if (isHour) cell.getStyleClass().add("cal-slot-hour");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                calendarGrid.add(cell, d + 1, slot + 1);
            }
        }

        // Group reservations by day offset
        Map<Integer, List<Reservation>> byDay = new HashMap<>();
        for (Reservation r : reservations) {
            int colOffset = (int) ChronoUnit.DAYS.between(weekStart, LocalDate.parse(r.getDate()));
            if (colOffset >= 0 && colOffset < 7) {
                byDay.computeIfAbsent(colOffset, k -> new ArrayList<>()).add(r);
            }
        }

        Long myId = SessionManager.getCurrentUser().getId();

        // One transparent overlay pane per day column, spanning all slot rows
        for (int d = 0; d < 7; d++) {
            final LocalDate day = weekStart.plusDays(d);
            List<Reservation> dayRes = byDay.getOrDefault(d, Collections.emptyList());

            // Sort by start time for overlap assignment
            List<Reservation> sorted = dayRes.stream()
                    .sorted(Comparator.comparingInt(r -> toMinutes(r.getStartTime())))
                    .collect(Collectors.toList());

            Pane overlay = new Pane();
            overlay.getStyleClass().add("cal-overlay");
            overlay.setPrefHeight(NUM_SLOTS * ROW_H);
            overlay.setMinWidth(0);
            overlay.setPrefWidth(0);
            overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Clip so card labels don't push column width
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(overlay.widthProperty());
            clip.heightProperty().bind(overlay.heightProperty());
            overlay.setClip(clip);

            // Drag on empty area → select time range and open create dialog
            Region dragPreview = new Region();
            dragPreview.getStyleClass().add("cal-drag-preview");
            dragPreview.setVisible(false);
            dragPreview.setMouseTransparent(true);
            overlay.getChildren().add(dragPreview);

            final double[] dragStartY = {-1};

            overlay.setOnMousePressed(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    // Close any open context menu
                    if (activeContextMenu != null) {
                        activeContextMenu.hide();
                        activeContextMenu = null;
                    }
                    dragStartY[0] = event.getY();
                    int slot = Math.max(0, Math.min(NUM_SLOTS - 1, (int) (event.getY() / ROW_H)));
                    double snapY = slot * ROW_H;
                    dragPreview.setLayoutX(0);
                    dragPreview.setLayoutY(snapY);
                    dragPreview.setPrefHeight(ROW_H);
                    dragPreview.prefWidthProperty().bind(overlay.widthProperty());
                    dragPreview.setVisible(true);
                }
            });

            overlay.setOnMouseDragged(event -> {
                if (dragStartY[0] >= 0) {
                    int startSlot = Math.max(0, Math.min(NUM_SLOTS - 1, (int) (dragStartY[0] / ROW_H)));
                    int endSlot = Math.max(0, Math.min(NUM_SLOTS, (int) Math.ceil(event.getY() / ROW_H)));
                    if (endSlot <= startSlot) endSlot = startSlot + 1;
                    endSlot = Math.min(endSlot, NUM_SLOTS);
                    double snapStartY = startSlot * ROW_H;
                    double h = (endSlot - startSlot) * ROW_H;
                    dragPreview.setLayoutY(snapStartY);
                    dragPreview.setPrefHeight(h);
                }
            });

            overlay.setOnMouseReleased(event -> {
                if (dragStartY[0] >= 0 && event.getButton() == MouseButton.PRIMARY) {
                    dragPreview.setVisible(false);
                    int startSlot = Math.max(0, Math.min(NUM_SLOTS - 1, (int) (dragStartY[0] / ROW_H)));
                    int endSlot = Math.max(0, Math.min(NUM_SLOTS, (int) Math.ceil(event.getY() / ROW_H)));
                    if (endSlot <= startSlot) endSlot = startSlot + 1;
                    endSlot = Math.min(endSlot, NUM_SLOTS);
                    int startMins = DAY_START + startSlot * SLOT_MIN;
                    int endMins = DAY_START + endSlot * SLOT_MIN;
                    dragStartY[0] = -1;
                    openCreateReservationPrefilled(day,
                            String.format("%02d:%02d", startMins / 60, startMins % 60),
                            String.format("%02d:%02d", endMins / 60, endMins % 60));
                }
            });

            // Assign sub-columns to handle side-by-side overlapping events
            int[] colAssignment = assignOverlapColumns(sorted);
            int totalCols = sorted.isEmpty() ? 1 : Arrays.stream(colAssignment).max().orElse(0) + 1;

            for (int i = 0; i < sorted.size(); i++) {
                Reservation r = sorted.get(i);
                String status = r.getOrganizerId().equals(myId)
                        ? "organizer"
                        : myStatuses.getOrDefault(r.getId(), "pending");

                int startMin = toMinutes(r.getStartTime());
                int endMin   = toMinutes(r.getEndTime());
                int startSlot = Math.max(0, (startMin - DAY_START) / SLOT_MIN);
                int endSlot   = Math.min(NUM_SLOTS, (int) Math.ceil((double) (endMin - DAY_START) / SLOT_MIN));
                int rowSpan   = Math.max(1, endSlot - startSlot);

                double yPos  = startSlot * ROW_H + 2;
                double height = rowSpan * ROW_H - 4;

                int cardCol = colAssignment[i];
                VBox card = buildEventCard(r, rowSpan, status);
                card.setLayoutY(yPos);
                card.setPrefHeight(height);
                card.setMaxHeight(height);
                card.setMinHeight(height);

                // Bind X and width to the overlay's width, divided by number of sub-columns
                card.layoutXProperty().bind(
                        overlay.widthProperty().divide(totalCols).multiply(cardCol).add(2));
                card.prefWidthProperty().bind(
                        overlay.widthProperty().divide(totalCols).subtract(4));

                final Reservation fr = r;
                final String fs = status;
                card.setOnMousePressed(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        if (activeContextMenu != null) {
                            activeContextMenu.hide();
                        }
                        activeContextMenu = buildEventContextMenu(fr, fs);
                        activeContextMenu.show(card, event.getScreenX(), event.getScreenY());
                    }
                    event.consume(); // prevent event from reaching the overlay
                });

                overlay.getChildren().add(card);
            }

            GridPane.setRowSpan(overlay, NUM_SLOTS);
            GridPane.setFillWidth(overlay, true);
            GridPane.setFillHeight(overlay, true);
            calendarGrid.add(overlay, d + 1, 1);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Event card                                                          */
    /* ------------------------------------------------------------------ */

    private VBox buildEventCard(Reservation r, int rowSpan, String status) {
        VBox card = new VBox(2);
        card.getStyleClass().add("cal-event");
        if ("declined".equals(status)) {
            card.getStyleClass().add("cal-event-declined");
        } else if ("pending".equals(status)) {
            card.getStyleClass().add("cal-event-pending");
        }
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label title = new Label(r.getTitle());
        title.getStyleClass().add("cal-event-title");
        if ("declined".equals(status)) title.getStyleClass().add("cal-event-title-declined");
        title.setWrapText(true);
        card.getChildren().add(title);

        Label time = new Label(r.getStartTime() + " – " + r.getEndTime());
        time.getStyleClass().add("cal-event-detail");
        card.getChildren().add(time);

        if (rowSpan >= 2) {
            Room room = roomById.get(r.getRoomId());
            if (room != null) {
                Label roomLabel = new Label("📍 " + room.getName());
                roomLabel.getStyleClass().add("cal-event-detail");
                card.getChildren().add(roomLabel);
            }
        }

        if (rowSpan >= 3) {
            List<Long> ids = reservationService.getParticipantIds(r.getId());
            if (!ids.isEmpty()) {
                String names = ids.stream()
                        .limit(3)
                        .map(id -> {
                            User u = userById.get(id);
                            return u != null ? u.getFirstName() + " " + u.getLastName().charAt(0) + "." : "?";
                        })
                        .collect(Collectors.joining(", "));
                if (ids.size() > 3) names += " +" + (ids.size() - 3);
                Label pl = new Label("👥 " + names);
                pl.getStyleClass().add("cal-event-detail");
                pl.setWrapText(true);
                card.getChildren().add(pl);
            }
        }

        return card;
    }

    /* ------------------------------------------------------------------ */
    /*  Event context menu                                                  */
    /* ------------------------------------------------------------------ */

    private ContextMenu buildEventContextMenu(Reservation r, String status) {
        ContextMenu menu = new ContextMenu();
        Long myId = SessionManager.getCurrentUser().getId();

        if (r.getOrganizerId().equals(myId)) {
            MenuItem edit = new MenuItem("Modifier");
            edit.setOnAction(e -> openEditReservation(r));

            MenuItem cancel = new MenuItem("Annuler la réunion");
            cancel.setStyle("-fx-text-fill: #dc2626;");
            cancel.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Annuler la réunion \"" + r.getTitle() + "\" ?",
                        ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        reservationService.cancelReservation(r.getId());
                        showWeek(currentWeekStart);
                    }
                });
            });

            menu.getItems().addAll(edit, new SeparatorMenuItem(), cancel);
        } else {
            if (!"accepted".equals(status)) {
                MenuItem accept = new MenuItem("Accepter");
                accept.setOnAction(e -> {
                    reservationService.acceptReservation(r.getId());
                    showWeek(currentWeekStart);
                });
                menu.getItems().add(accept);
            }
            if (!"declined".equals(status)) {
                MenuItem decline = new MenuItem("Refuser");
                decline.setOnAction(e -> {
                    reservationService.declineReservation(r.getId());
                    showWeek(currentWeekStart);
                });
                menu.getItems().add(decline);
            }
        }

        return menu;
    }

    /* ------------------------------------------------------------------ */
    /*  Open dialogs                                                        */
    /* ------------------------------------------------------------------ */

    private void openCreateReservationPrefilled(LocalDate date, String startTime, String endTime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createReservation.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nouvelle réunion");
            stage.setScene(new Scene(loader.load(), 500, 550));
            stage.setMinWidth(460);
            stage.setMinHeight(400);
            stage.sizeToScene();
            stage.setOnHidden(e -> showWeek(currentWeekStart));
            CreateReservationController controller = loader.getController();
            controller.prefill(date, startTime, endTime);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditReservation(Reservation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editReservation.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Détail de la réunion");
            stage.setScene(new Scene(loader.load(), 540, 620));
            stage.setMinWidth(480);
            stage.setMinHeight(450);
            stage.sizeToScene();
            stage.setOnHidden(e -> showWeek(currentWeekStart));
            EditReservationController controller = loader.getController();
            controller.setReservation(r);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Overlap column assignment                                           */
    /* ------------------------------------------------------------------ */

    private int[] assignOverlapColumns(List<Reservation> sorted) {
        if (sorted.isEmpty()) return new int[0];
        int[] cols = new int[sorted.size()];
        List<Integer> colEnds = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            int start = toMinutes(sorted.get(i).getStartTime());
            int end   = toMinutes(sorted.get(i).getEndTime());

            int assignedCol = -1;
            for (int c = 0; c < colEnds.size(); c++) {
                if (colEnds.get(c) <= start) {
                    assignedCol = c;
                    colEnds.set(c, end);
                    break;
                }
            }
            if (assignedCol == -1) {
                assignedCol = colEnds.size();
                colEnds.add(end);
            }
            cols[i] = assignedCol;
        }
        return cols;
    }

    /* ------------------------------------------------------------------ */
    /*  Utilities                                                           */
    /* ------------------------------------------------------------------ */

    private int toMinutes(String time) {
        String[] p = time.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private String formatWeekRange(LocalDate from, LocalDate to) {
        DateTimeFormatter f  = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);
        DateTimeFormatter fy = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH);
        if (from.getYear() == to.getYear()) {
            return "Semaine du " + from.format(f) + " au " + to.format(fy);
        }
        return from.format(fy) + " – " + to.format(fy);
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
