package me.cable.dm.minigame;

import me.cable.dm.DynamicMinigames;
import me.cable.dm.leaderboard.Leaderboard;
import org.bukkit.NamespacedKey;
import me.cable.dm.MinigameManager;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.*;

public abstract class Minigame {

    private static boolean initialized;

    private final Map<String, AbstractOption> options = new LinkedHashMap<>(); // LinkedHashMap for order
    private final Map<String, Supplier<List<Leaderboard.Score>>> registeredLeaderboards = new LinkedHashMap<>();
    private final List<BukkitTask> activeTasks = new ArrayList<>();

    private @Nullable List<Leaderboard> leaderboards;
    private @Nullable ConfigurationSection leaderboardsConfigurationSection;

    public static void initializeTimer(@NotNull DynamicMinigames dynamicMinigames) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        MinigameManager minigameManager = dynamicMinigames.getMinigameManager();
        initialized = true;

        // tick tasks
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

        // leaderboard tasks
        Bukkit.getScheduler().scheduleSyncRepeatingTask(dynamicMinigames, () -> {
            for (Minigame minigame : minigameManager.getAllMinigames()) {
                minigame.updateLeaderboards();
            }
        }, 0, 20 * 60);
    }

    public final void initializeLeaderboards(@NotNull ConfigurationSection leaderboardsCs) {
        if (leaderboardsConfigurationSection != null) {
            throw new IllegalStateException("Leaderboards already initialized");
        }

        leaderboardsConfigurationSection = leaderboardsCs;
        leaderboards = new ArrayList<>();

        for (Map.Entry<String, Supplier<List<Leaderboard.Score>>> entry : registeredLeaderboards.entrySet()) {
            ConfigurationSection leaderboardCs = getLeaderboardCs(leaderboardsCs, entry.getKey());
            Leaderboard leaderboard = new Leaderboard(leaderboardCs, entry.getValue());
            leaderboards.add(leaderboard);
        }

        updateLeaderboards();
    }

    public final void updateLeaderboards() {
        if (leaderboards != null) leaderboards.forEach(Leaderboard::update);
    }

    public final void removeLeaderboards() {
        if (leaderboards != null) leaderboards.forEach(Leaderboard::remove);
    }

    public final <T extends AbstractOption> @NotNull T registerOption(@NotNull String id, @NotNull T option) {
        if (options.containsKey(id)) {
            throw new IllegalArgumentException("Option with ID already exists: " + id);
        }

        options.put(id, option);
        return option;
    }

    public final void registerLeaderboard(@NotNull String id, @NotNull Supplier<List<Leaderboard.Score>> scoreSupplier) {
        registeredLeaderboards.put(id, scoreSupplier);
    }

    protected final @NotNull NamespacedKey getNamespacedKey(@NotNull String key) {
        return new NamespacedKey(JavaPlugin.getProvidingPlugin(DynamicMinigames.class), key);
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

    public @NotNull ConfigurationSection getLeaderboardsConfigurationSection() {
        if (leaderboardsConfigurationSection == null) {
            throw new IllegalStateException("Leaderboards not initialized");
        }

        return leaderboardsConfigurationSection;
    }

    private @NotNull ConfigurationSection getLeaderboardCs(@NotNull ConfigurationSection leaderboardsCs, @NotNull String id) {
        ConfigurationSection leaderboardCs = leaderboardsCs.getConfigurationSection(id);

        if (leaderboardCs == null) leaderboardCs = leaderboardsCs.createSection(id);
        if (!leaderboardCs.contains("enabled")) leaderboardCs.set("enabled", false);
        if (!leaderboardCs.contains("position")) leaderboardCs.set("position", "world,0.0,0.0,0.0");
        if (!leaderboardCs.contains("title")) leaderboardCs.set("title", "&6&lLeaderboard Title");
        if (!leaderboardCs.contains("format")) leaderboardCs.set("format", "&6{position}. {player} - {score}");
        if (!leaderboardCs.contains("entries")) leaderboardCs.set("entries", 10);
        if (!leaderboardCs.contains("exclude")) leaderboardCs.set("exclude", List.of("player_uuid"));

        return leaderboardCs;
    }
}
