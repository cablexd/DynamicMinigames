package me.cable.dm.commands;

import me.cable.dm.DynamicMinigames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCommand implements TabExecutor {

    protected final DynamicMinigames dynamicMinigames;

    public AbstractCommand() {
        dynamicMinigames = JavaPlugin.getPlugin(DynamicMinigames.class);
    }

    public void register(@NotNull String label) {
        PluginCommand pluginCommand = dynamicMinigames.getCommand(label);

        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return Collections.emptyList();
    }
}
