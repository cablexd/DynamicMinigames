package me.cable.dm.leaderboard;

import me.cable.dm.DynamicMinigames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaderboardData {

    private final DynamicMinigames dynamicMinigames;

    public LeaderboardData(@NotNull DynamicMinigames dynamicMinigames) {
        this.dynamicMinigames = dynamicMinigames;
    }

    private @NotNull File getFile(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId) {
        return dynamicMinigames.getDataFolder().toPath()
                .resolve("Leaderboard Data/" + minigameType + "/" + minigameId + "/" + leaderboardId + ".yml").toFile();
    }

    private @NotNull YamlConfiguration getConfig(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId) {
        File file = getFile(minigameType, minigameId, leaderboardId);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveConfig(@NotNull FileConfiguration config, @NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId) {
        File file = getFile(minigameType, minigameId, leaderboardId);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public @Nullable Long getValue(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId,
                                   @NotNull UUID playerUuid) {
        FileConfiguration config = getConfig(minigameType, minigameId, leaderboardId);
        return config.isSet(playerUuid.toString()) ? config.getLong(playerUuid.toString()) : null;
    }

    public @NotNull Map<UUID, Long> getValues(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId) {
        FileConfiguration config = getConfig(minigameType, minigameId, leaderboardId);
        Map<UUID, Long> map = new HashMap<>();

        for (String key : config.getKeys(false)) {
            map.put(UUID.fromString(key), config.getLong(key));
        }

        return map;
    }

    public void setValue(@NotNull String minigameType, @NotNull String minigameId, @NotNull String leaderboardId,
                         @NotNull UUID playerUuid, @Nullable Long value) {
        FileConfiguration config = getConfig(minigameType, minigameId, leaderboardId);
        config.set(playerUuid.toString(), value);
        saveConfig(config, minigameType, minigameId, leaderboardId);
    }
}
