package me.cable.dm.option.abs;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Option<T> {

    public static final String USE_DEFAULT = "$default";

    private @Nullable T value;
    private @Nullable T defaultValue;

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

    public @NotNull T get() {
        if (value != null) {
            return value;
        }
        if (defaultValue != null) {
            return defaultValue;
        }

        throw new IllegalStateException("Option has no value");
    }

    public void set(@Nullable T value) {
        this.value = value;
    }

    /*
        Return null to use default value. Return Map<String, Object> to set to configuration section.
     */
    public abstract @Nullable Object serialize();

    /*
        Can provide a configuration section.
     */
    public @Nullable T deserialize(@NotNull Object object) {
        return (object instanceof ConfigurationSection configurationSection) ? deserialize(configurationSection) : null;
    }

    public @Nullable T deserialize(@NotNull ConfigurationSection configurationSection) {
        return null;
    }

    public final @Nullable T _get() {
        return value;
    }

    public final void _set(@Nullable Object value) {
        this.value = (T) value;
    }

    public final @Nullable T _getDefault() {
        return defaultValue;
    }

    public final void _setDefault(@Nullable Object value) {
        this.defaultValue = (T) value;
    }
}
