package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerOption extends Option<Integer> {

    @Override
    public boolean canSetInGame() {
        return true;
    }

    @Override
    public @Nullable String setInGame(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length < 1) {
            return " <integer_value>";
        }

        try {
            int val = Integer.parseInt(args[0]);
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
    public @Nullable Integer deserialize(@NotNull Object object) {
        return (object instanceof Integer i) ? i : null;
    }
}
