package me.cable.dm.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class Utils {

    public static @NotNull String formatColor(@NotNull String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static @NotNull String formatMillis(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis / 1000) % 60;
        long milliseconds = millis % 1000;
        return minutes + "m:" + seconds + "s:" + milliseconds + "ms";
    }
}
