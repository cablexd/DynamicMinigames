package me.cable.dm.util;

import me.cable.dm.DynamicMinigames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PlayerTextInput implements Listener {

    private final Player player;
    private final Function<String, Boolean> onInput;
    private final @Nullable Runnable onCancel;

    private boolean listeningStarted;

    public PlayerTextInput(@NotNull Player player, @NotNull Function<String, Boolean> onInput, @Nullable Runnable onCancel) {
        this.player = player;
        this.onInput = onInput;
        this.onCancel = onCancel;
    }

    public PlayerTextInput(@NotNull Player player, @NotNull Function<String, Boolean> onInput) {
        this(player, onInput, null);
    }

    public void listen() {
        if (listeningStarted) {
            throw new IllegalStateException("Listening has already started");
        }

        listeningStarted = true;
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getPlugin(DynamicMinigames.class));
    }

    @EventHandler
    private void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent e) {
        if (e.getPlayer() != player) return;

        String message = e.getMessage();
        e.setCancelled(true);

        if (onCancel != null && message.equals("cancel")) {
            HandlerList.unregisterAll(this);
            onCancel.run();
        } else if (onInput.apply(message)) {
            HandlerList.unregisterAll(this);
        }
    }
}
