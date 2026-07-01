package org.example.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Systeme de dialogues modaux affiches en superposition (overlay) dans la scene courante.
 *
 * <p>Contrairement a l'ouverture d'un {@link Stage} separe, un overlay reste dans la fenetre
 * principale : le comportement est donc identique sur toutes les plateformes, y compris en
 * plein ecran sur macOS ou une nouvelle fenetre ouvrirait un espace separe.</p>
 *
 * <p>Chaque modal est ferme par la touche ECHAP, un clic sur le fond assombri, le bouton de
 * fermeture, ou un appel a {@link #close(Node)} depuis le controleur du contenu.</p>
 */
public final class Modals {

    private static final String OVERLAY_CLASS = "modal-overlay";
    private static final String ON_CLOSE_KEY = "modals.onClose";
    private static final String ESC_HANDLER_KEY = "modals.escHandler";

    private Modals() {
    }

    /**
     * Ouvre un fichier FXML dans un modal en superposition de la scene courante.
     *
     * @param context   n'importe quel noeud deja attache a la scene
     * @param fxmlPath  chemin ressource du FXML (ex : "/fxml/createTask.fxml")
     * @param maxWidth  largeur maximale du contenu
     * @param maxHeight hauteur maximale du contenu
     * @param onClose   callback execute apres fermeture (peut etre {@code null})
     * @param <T>       type du controleur FXML
     * @return le controleur du FXML charge
     */
    public static <T> T open(Node context, String fxmlPath, double maxWidth, double maxHeight,
                             Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(Modals.class.getResource(fxmlPath));
            Parent content = loader.load();
            show(context.getScene(), content, maxWidth, maxHeight, onClose, true);
            return loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger le modal : " + fxmlPath, e);
        }
    }

    /** Ferme le modal contenant le noeud donne (ou la fenetre si le noeud est dans un Stage separe). */
    public static void close(Node nodeInsideModal) {
        Node current = nodeInsideModal;
        while (current != null) {
            if (current.getStyleClass().contains(OVERLAY_CLASS) && current instanceof StackPane overlay) {
                dismiss(overlay);
                return;
            }
            current = current.getParent();
        }
        // Securite : si le contenu a ete ouvert dans une fenetre classique.
        if (nodeInsideModal.getScene() != null
                && nodeInsideModal.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /** Boite de confirmation (remplace Alert CONFIRMATION) avec style applicatif. */
    public static void confirm(Node context, String title, String message, Runnable onConfirm) {
        confirm(context, title, message, "Confirmer", false, onConfirm);
    }

    /** Boite de confirmation pour action destructrice (bouton rouge). */
    public static void confirmDelete(Node context, String message, Runnable onConfirm) {
        confirm(context, "Confirmation", message, "Supprimer", true, onConfirm);
    }

    /**
     * Boite de confirmation generique.
     *
     * @param destructive si vrai, le bouton de confirmation est affiche en rouge
     */
    public static void confirm(Node context, String title, String message,
                               String confirmText, boolean destructive, Runnable onConfirm) {
        VBox card = buildMessageCard(title, message);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.getStyleClass().add("btn-back");

        Button okBtn = new Button(confirmText);
        okBtn.getStyleClass().add(destructive ? "btn-danger" : "btn-primary");

        HBox buttons = new HBox(10, cancelBtn, okBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().add(buttons);

        StackPane overlay = show(context.getScene(), card, 440, 300, null, false);
        cancelBtn.setOnAction(e -> dismiss(overlay));
        okBtn.setOnAction(e -> {
            dismiss(overlay);
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        okBtn.requestFocus();
    }

    /** Message d'information (remplace Alert INFORMATION). */
    public static void info(Node context, String title, String message) {
        message(context, title, message, false);
    }

    /** Message d'erreur (remplace Alert ERROR). */
    public static void error(Node context, String message) {
        message(context, "Erreur", message, true);
    }

    private static void message(Node context, String title, String message, boolean isError) {
        VBox card = buildMessageCard(title, message);
        if (isError) {
            card.getStyleClass().add("modal-card-error");
        }

        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("btn-primary");
        HBox buttons = new HBox(okBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().add(buttons);

        StackPane overlay = show(context.getScene(), card, 440, 300, null, false);
        okBtn.setOnAction(e -> dismiss(overlay));
        okBtn.requestFocus();
    }

    private static VBox buildMessageCard(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("modal-message");

        VBox card = new VBox(16, titleLabel, messageLabel);
        card.getStyleClass().addAll("dialog-card", "modal-card");
        card.setMaxWidth(440);
        return card;
    }

    /* ------------------------------------------------------------------ */
    /*  Coeur du systeme d'overlay                                          */
    /* ------------------------------------------------------------------ */

    private static StackPane show(Scene scene, Parent content, double maxWidth, double maxHeight,
                                  Runnable onClose, boolean withCloseButton) {
        StackPane host = hostFor(scene);

        Node card;
        if (withCloseButton) {
            Button closeBtn = new Button("✕");
            closeBtn.getStyleClass().add("modal-close-btn");

            StackPane wrapper = new StackPane(content, closeBtn);
            StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10, 10, 0, 0));
            wrapper.getStyleClass().add("modal-card");
            wrapper.setMaxSize(maxWidth, maxHeight);
            card = wrapper;

            closeBtn.setOnAction(e -> {
                Node overlayNode = wrapper.getParent();
                if (overlayNode instanceof StackPane overlay) {
                    dismiss(overlay);
                }
            });
        } else {
            if (content instanceof Region region) {
                region.setMaxSize(maxWidth, Region.USE_PREF_SIZE);
            }
            card = content;
        }

        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add(OVERLAY_CLASS);
        overlay.setAlignment(Pos.CENTER);
        if (onClose != null) {
            overlay.getProperties().put(ON_CLOSE_KEY, onClose);
        }

        // Clic sur le fond assombri (hors carte) : fermeture.
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                dismiss(overlay);
            }
        });

        // ECHAP : ne ferme que le modal le plus haut (les filtres de tous les
        // overlays ouverts recoivent l'evenement, meme consomme).
        javafx.event.EventHandler<KeyEvent> escHandler = e -> {
            if (e.getCode() == KeyCode.ESCAPE && isTopmost(overlay)) {
                dismiss(overlay);
                e.consume();
            }
        };
        overlay.getProperties().put(ESC_HANDLER_KEY, escHandler);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, escHandler);

        host.getChildren().add(overlay);

        // Animation d'ouverture : fondu + leger zoom.
        overlay.setOpacity(0);
        card.setScaleX(0.94);
        card.setScaleY(0.94);

        FadeTransition fade = new FadeTransition(Duration.millis(160), overlay);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(160), card);
        scale.setToX(1);
        scale.setToY(1);
        new ParallelTransition(fade, scale).play();

        card.requestFocus();
        return overlay;
    }

    private static boolean isTopmost(StackPane overlay) {
        if (!(overlay.getParent() instanceof StackPane host)) {
            return false;
        }
        for (int i = host.getChildren().size() - 1; i >= 0; i--) {
            Node child = host.getChildren().get(i);
            if (child.getStyleClass().contains(OVERLAY_CLASS)) {
                return child == overlay;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void dismiss(StackPane overlay) {
        // Evite une double fermeture (ECHAP pendant l'animation, par exemple).
        if (overlay.getProperties().containsKey("modals.closing")) {
            return;
        }
        overlay.getProperties().put("modals.closing", Boolean.TRUE);

        Scene scene = overlay.getScene();
        if (scene != null) {
            Object handler = overlay.getProperties().get(ESC_HANDLER_KEY);
            if (handler instanceof javafx.event.EventHandler) {
                scene.removeEventFilter(KeyEvent.KEY_PRESSED,
                        (javafx.event.EventHandler<KeyEvent>) handler);
            }
        }

        Runnable onClose = (Runnable) overlay.getProperties().get(ON_CLOSE_KEY);
        FadeTransition fade = new FadeTransition(Duration.millis(120), overlay);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            if (overlay.getParent() instanceof StackPane parent) {
                parent.getChildren().remove(overlay);
            }
            if (onClose != null) {
                onClose.run();
            }
        });
        fade.play();
    }

    /**
     * Retourne le conteneur d'overlays de la scene. Si la racine actuelle n'est pas encore
     * un hote de modaux, elle est enveloppee dans un StackPane qui devient la nouvelle racine.
     */
    private static StackPane hostFor(Scene scene) {
        Parent root = scene.getRoot();
        if (root instanceof StackPane stack && stack.getStyleClass().contains("modal-host")) {
            return stack;
        }
        StackPane host = new StackPane();
        host.getStyleClass().add("modal-host");
        scene.setRoot(host);
        host.getChildren().add(root);
        return host;
    }
}
