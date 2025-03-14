package me.cable.dm.minigame;

import me.cable.dm.DynamicMinigames;
import me.cable.dm.MinigameManager;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Minigame {

    private static boolean initialized;

    private final Map<String, AbstractOption> options = new LinkedHashMap<>(); // LinkedHashMap for order
    private final List<BukkitTask> activeTasks = new ArrayList<>();

    public static void initializeTimer(@NotNull DynamicMinigames dynamicMinigames) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        MinigameManager minigameManager = dynamicMinigames.getMinigameManager();
        initialized = true;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(dynamicMinigames, () -> {
            for (Minigame minigame : minigameManager.getAllMinigames()) {
                // remove all cancelled tasks
                minigame.activeTasks.removeIf(a -> !Bukkit.getScheduler().isQueued(a.getTaskId())); // includes cancelled

                // call tick methods
                if (minigame instanceof IntermissionMinigame intermissionMinigame) {
                    if (intermissionMinigame.getGameState() == IntermissionMinigame.GameState.RUNNING) {
                        intermissionMinigame.tick();
                    }
                } else if (minigame instanceof PassiveMinigame passiveMinigame) {
                    passiveMinigame.tick();
                }
            }
        }, 0, 1);
    }

    public final <T extends AbstractOption> @NotNull T registerOption(@NotNull String id, @NotNull T option) {
        if (options.containsKey(id)) {
            throw new IllegalArgumentException("Option with ID already exists: " + id);
        }

        options.put(id, option);
        return option;
    }

    protected final @NotNull BukkitTask runTaskTimer(long delay, long period, @NotNull BukkitRunnable bukkitRunnable) {
        BukkitTask bukkitTask = bukkitRunnable.runTaskTimer(JavaPlugin.getProvidingPlugin(DynamicMinigames.class), delay, period);
        activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    protected final @NotNull BukkitTask runTaskTimer(long delay, long period, @NotNull Runnable runnable) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getProvidingPlugin(DynamicMinigames.class), runnable, delay, period);
        activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    protected final @NotNull BukkitTask runTaskLater(long delay, @NotNull Runnable runnable) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(DynamicMinigames.class), runnable, delay);
        activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    public final void stopTasks() {
        activeTasks.forEach(BukkitTask::cancel);
        activeTasks.clear();
    }

    public void onOptionChange(@NotNull String optionId, @NotNull AbstractOption abstractOption) {
        // TODO: call
    }

    public final @NotNull Map<String, AbstractOption> getOptions() {
        return new LinkedHashMap<>(options);
    }
}
