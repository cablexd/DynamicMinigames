package me.cable.dm.minigame;

import me.cable.dm.DynamicMinigames;
import me.cable.dm.leaderboard.Leaderboard;
import org.bukkit.NamespacedKey;
import me.cable.dm.MinigameManager;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.*;

public abstract class Minigame {

    private static boolean initialized;

    private final Map<String, AbstractOption> options = new LinkedHashMap<>(); // LinkedHashMap for order
    private final Map<String, Leaderboard> leaderboards = new LinkedHashMap<>(); // LinkedHashMap for order
    private final List<BukkitTask> activeTasks = new ArrayList<>();

    private @Nullable String typeId;
    private @Nullable String id;

    public static void initializeTimer(@NotNull DynamicMinigames dynamicMinigames) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        MinigameManager minigameManager = dynamicMinigames.getMinigameManager();
        initialized = true;

        // tick tasks
        Bukkit.getScheduler().scheduleSyncRepeatingTask(dynamicMinigames, () -> {
            for (Minigame minigame : minigameManager.getMinigamesList()) {
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
            for (Minigame minigame : minigameManager.getMinigamesList()) {
                minigame.updateLeaderboards();
            }
        }, 0, 20 * 60);
    }

    public void initialize(@NotNull String typeId, @NotNull String id) {
        if (this.typeId != null) {
            throw new IllegalStateException("Minigame already initialized");
        }

        this.typeId = typeId;
        this.id = id;
    }

    public final void updateLeaderboards() {
        leaderboards.values().forEach(Leaderboard::update);
    }

    public final void removeLeaderboards() {
        leaderboards.values().forEach(Leaderboard::remove);
    }

    public @Nullable Long getLeaderboardScore(@NotNull String leaderboardId, @NotNull UUID playerUuid) {
        return JavaPlugin.getPlugin(DynamicMinigames.class).getLeaderboardData().getValue(getTypeId(), getId(), leaderboardId, playerUuid);
    }

    public void setLeaderboardScore(@NotNull String leaderboardId, @NotNull UUID playerUuid, @Nullable Long score) {
        JavaPlugin.getPlugin(DynamicMinigames.class).getLeaderboardData().setValue(getTypeId(), getId(), leaderboardId, playerUuid, score);
    }

    public final <T extends AbstractOption> @NotNull T registerOption(@NotNull String id, @NotNull T option) {
        if (options.containsKey(id)) {
            throw new IllegalArgumentException("Option with ID already exists: " + id);
        }

        options.put(id, option);
        return option;
    }

    public final void registerLeaderboard(@NotNull String id, @NotNull Function<Long, String> scoreFormatter, @NotNull Comparator<Long> scoreSorter) {
        leaderboards.put(id, new Leaderboard(this, scoreFormatter, scoreSorter));
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

    public @NotNull Map<String, Leaderboard> getLeaderboards() {
        return new LinkedHashMap<>(leaderboards);
    }

    public @NotNull String getTypeId() {
        return Objects.requireNonNull(typeId, "Minigame not initialized");
    }

    public @NotNull String getId() {
        return Objects.requireNonNull(id, "Minigame not initialized");
    }
}
