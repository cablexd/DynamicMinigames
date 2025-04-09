package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.PlayerTextInput;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringOption extends Option<String> {

    @Override
    public boolean canSetInGame() {
        return true;
    }

    @Override
    public @Nullable String setInGame(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage(ChatColor.GREEN + "Type the new value in chat or type \"cancel\" to cancel:");

        new PlayerTextInput(player, input -> {
            set(input);
            player.sendMessage(ChatColor.GREEN + "Set value to: " + ChatColor.GOLD + input);
            return true;
        }, () -> player.sendMessage(ChatColor.GREEN + "Input cancelled.")).listen();

        return null;
    }

    @Override
    public @Nullable Object serialize() {
        return _get();
    }

    @Override
    public @Nullable String deserialize(@NotNull Object object) {
        return (object instanceof String s) ? s : null;
    }
}