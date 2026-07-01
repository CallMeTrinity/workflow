package org.example.ui.util;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Gestion du theme clair / sombre de l'application.
 *
 * <p>Le theme sombre est active en ajoutant la classe de style {@code theme-dark} a la racine
 * de la scene ; les couleurs sont definies via des "looked-up colors" dans app.css.
 * Le choix de l'utilisateur est persiste entre les lancements via {@link Preferences}.</p>
 */
public final class ThemeManager {

    private static final String DARK_CLASS = "theme-dark";
    private static final String PREF_KEY = "darkTheme";
    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {
    }

    /** Indique si le theme sombre est actif. */
    public static boolean isDark() {
        return PREFS.getBoolean(PREF_KEY, false);
    }

    /**
     * Enregistre la scene : applique le theme courant et le re-applique automatiquement
     * a chaque changement de racine (navigation entre les vues).
     */
    public static void register(Scene scene) {
        apply(scene);
        scene.rootProperty().addListener((obs, oldRoot, newRoot) -> apply(scene));
    }

    /** Bascule clair / sombre et persiste le choix. */
    public static void toggle(Scene scene) {
        PREFS.putBoolean(PREF_KEY, !isDark());
        apply(scene);
    }

    private static void apply(Scene scene) {
        if (scene.getRoot() == null) {
            return;
        }
        var classes = scene.getRoot().getStyleClass();
        if (isDark()) {
            if (!classes.contains(DARK_CLASS)) {
                classes.add(DARK_CLASS);
            }
        } else {
            classes.remove(DARK_CLASS);
        }
    }
}
