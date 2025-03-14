package me.cable.dm.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class Utils {

    public static @NotNull String formatColor(@NotNull String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
