package me.cable.dm.leaderboard;

import me.cable.dm.DynamicMinigames;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class LeaderboardData {

    private final DynamicMinigames dynamicMinigames;

    public LeaderboardData(@NotNull DynamicMinigames dynamicMinigames) {
        this.dynamicMinigames = dynamicMinigames;
    }

    private void useFile(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId, @NotNull Function<YamlConfiguration, Boolean> f) {
        File file = dynamicMinigames.getDataFolder().toPath()
                .resolve("Leaderboard Data/" + minigameType + "/" + minigameId + "/" + leaderboardId + ".yml").toFile();

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // save if consumer returns true
        if (f.apply(config)) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public @Nullable Integer getValue(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId,
                                      @NotNull UUID playerUuid) {
        int[] v = new int[1];

        useFile(minigameType, minigameId, leaderboardId, config -> {
            v[0] = config.getInt(playerUuid.toString());
            return false;
        });

        return v[0];
    }

    public @NotNull Map<UUID, Integer> getValues(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId) {
        Map<UUID, Integer> map = new HashMap<>();

        useFile(minigameType, minigameId, leaderboardId, config -> {
            for (String key : config.getKeys(false)) {
                map.put(UUID.fromString(key), config.getInt(key));
            }

            return false;
        });

        return map;
    }

    public void setValue(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId,
                         @NotNull UUID playerUuid, @Nullable Integer value) {
        useFile(minigameType, minigameId, leaderboardId, config -> {
            config.set(playerUuid.toString(), value);
            return true;
        });
    }
}
