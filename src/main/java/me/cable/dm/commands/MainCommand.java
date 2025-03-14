package me.cable.dm.commands;

import me.cable.dm.MinigameSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainCommand extends AbstractCommand {

    private final MinigameSerializer minigameSerializer;

    public MainCommand() {
        minigameSerializer = dynamicMinigames.getMinigameSerializer();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.GREEN + "Hello, " + commandSender.getName() + "!");
            return true;
        }

        if (strings[0].equals("load")) {
            commandSender.sendMessage(ChatColor.GREEN + "Loading minigames...");
            minigameSerializer.loadMinigames();
        } else if (strings[0].equals("save")) {
            commandSender.sendMessage(ChatColor.GREEN + "Saving minigames...");
            minigameSerializer.saveMinigames();
        } else {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + s + " <load|save>");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> result = new ArrayList<>();

        if (strings.length == 1) {
            for (String string : List.of("load", "save")) {
                if (string.startsWith(strings[0])) {
                    result.add(string);
                }
            }
        }

        return result;
    }
}
