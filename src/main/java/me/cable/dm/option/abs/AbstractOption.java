package me.cable.dm.option.abs;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed abstract class AbstractOption permits Option, ListOption {

    public static final String USE_DEFAULT = "$default";

    public boolean canSetInGame() {
        return false;
    }

    public @Nullable String setInGame(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            return setInGame(player, args);
        }

        commandSender.sendMessage(ChatColor.RED + "Only players can modify this option!");
        return null;
    }

    public @Nullable String setInGame(@NotNull Player player, @NotNull String[] args) {
        return null;
    }

    public boolean useConfigurationSection() {
        return false;
    }

    /*
        Return null to use default value.
     */
    public @Nullable Object save() {
        return null;
    }

    /*
        Return true if value was saved. Return false to use default value.
     */
    public boolean save(@NotNull ConfigurationSection configurationSection) {
        return false;
    }

    public void load(@NotNull Object object) {
        // empty
    }

    public void load(@NotNull ConfigurationSection configurationSection) {
        // empty
    }
}
