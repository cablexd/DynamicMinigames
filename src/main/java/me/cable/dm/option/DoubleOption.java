package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleOption extends Option<Double> {

    @Override
    public boolean canSetInGame() {
        return true;
    }

    @Override
    public @Nullable String setInGame(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length < 1) {
            return " <number_value>";
        }

        try {
            double val = Double.parseDouble(args[0]);
            set(val);
            commandSender.sendMessage(ChatColor.GREEN + "Set value to: " + ChatColor.GOLD + val);
        } catch (NumberFormatException ex) {
            commandSender.sendMessage(ChatColor.RED + "Invalid value!");
        }

        return null;
    }

    @Override
    public @Nullable Object serialize() {
        return _get();
    }

    @Override
    public @Nullable Double deserialize(@NotNull Object object) {
        return (object instanceof Number n) ? n.doubleValue() : null;
    }
}
