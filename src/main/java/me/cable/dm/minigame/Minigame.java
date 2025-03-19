package me.cable.dm.minigame;

import me.cable.dm.DynamicMinigames;
import me.cable.dm.leaderboard.Leaderboard;
import org.bukkit.NamespacedKey;
import me.cable.dm.MinigameManager;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.*;

import org.bukkit.Location;

public abstract class Minigame {

    private static boolean initialized;

    private final Map<String, AbstractOption> options = new LinkedHashMap<>(); // LinkedHashMap for order
    private final Map<String, Leaderboard> leaderboards = new LinkedHashMap<>();
    private final List<BukkitTask> activeTasks = new ArrayList<>();

    private @Nullable ConfigurationSection leaderboardsCs;

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

    public final void registerLeaderboard(@NotNull String id, @NotNull Supplier<List<Score>> supplier, @NotNull Function<Integer, String> scoreFormatter) {
        if (leaderboardsCs == null || !leaderboardsCs.getBoolean(id + ".enabled")) {
            return;
        }

        Leaderboard leaderboard = new Leaderboard() {

            @Override
            public @Nullable String getTitle() {
                return leaderboardsCs.getString(id + ".title");
            }

            @Override
            public @NotNull List<Score> getScores() {
                List<Score> list = new ArrayList<>();
                List<Minigame.Score> scores = supplier.get();

                for (int i = 0; i < scores.size(); i++) {
                    String format = "&6{position}. {player} - {score}"; // TODO: get format from config
                    Minigame.Score score = scores.get(i);
                    String playerName = Bukkit.getOfflinePlayer(score.playerUuid).getName();

                    String formatted = format
                            .replace("{player}", playerName == null ? "N/A" : playerName)
                            .replace("{position}", Integer.toString(i + 1))
                            .replace("{score}", scoreFormatter.apply(score.value));
                    list.add(new Score(score.playerUuid, formatted));
                }

                return list;
            }
        };

        String locationStr = leaderboardsCs.getString(id + ".position");
        if (locationStr == null) return;

        String[] locParts = locationStr.split(",");
        Location location = new Location(
                Bukkit.getWorld(locParts[0]),
                Double.parseDouble(locParts[1]),
                Double.parseDouble(locParts[2]),
                Double.parseDouble(locParts[3])
        );

        leaderboard.setLocation(location);
        leaderboards.put(id, leaderboard);
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
}
