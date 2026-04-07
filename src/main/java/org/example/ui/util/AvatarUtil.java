package org.example.ui.util;

public class AvatarUtil {

    public static String generateColor(String pseudo) {
        String[] colors = {
                "#DC2626", "#991B1B", "#611b1b",
                "#BE123C", "#c41d87",
                "#fc4b03",
                "#e3911b",
                "#15803D", "#14532D", "#083b2d",
                "#1D4ED8", "#1E3A8A", "#172554",
                "#6D28D9", "#581C87"
        };

        if (pseudo == null || pseudo.isEmpty()) return "#999999";

        return colors[Math.abs(pseudo.hashCode()) % colors.length];
    }
}