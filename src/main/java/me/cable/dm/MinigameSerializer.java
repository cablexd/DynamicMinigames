package me.cable.dm;

import me.cable.dm.leaderboard.Leaderboard;
import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.minigame.Minigame;
import me.cable.dm.minigame.PassiveMinigame;
import me.cable.dm.option.abs.Option;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinigameSerializer {

    private final DynamicMinigames dynamicMinigames;
    private final MinigameManager minigameManager;

    private YamlConfiguration globalMinigameDefaults;
    private final Map<String, YamlConfiguration> minigameDefaults = new HashMap<>();

    public MinigameSerializer(@NotNull DynamicMinigames dynamicMinigames) {
        this.dynamicMinigames = dynamicMinigames;
        minigameManager = dynamicMinigames.getMinigameManager();
    }

    private @NotNull File getMinigamesDirectory() {
        return new File(dynamicMinigames.getDataFolder(), "Minigames");
    }

    private @Nullable ConfigurationSection getGlobalMinigameDefaultsConfig() {
        if (globalMinigameDefaults == null) {
            File file = new File(getMinigamesDirectory(), "defaults.yml");

            try {
                globalMinigameDefaults = YamlConfiguration.loadConfiguration(file);
            } catch (Exception e) {
                dynamicMinigames.getLogger().severe("Could not load global minigame defaults");
            }
        }

        return globalMinigameDefaults;
    }

    private @Nullable ConfigurationSection getMinigameDefaultsConfig(@NotNull String minigameType) {
        return minigameDefaults.computeIfAbsent(minigameType, t -> {
            File file = new File(new File(getMinigamesDirectory(), "Defaults"), minigameType + ".yml");

            try {
                return YamlConfiguration.loadConfiguration(file);
            } catch (Exception e) {
                dynamicMinigames.getLogger().severe("Could not load minigame defaults for " + minigameType);
                return null;
            }
        });
    }

    private @NotNull String formatKey(@NotNull String key) {
        return key.replace("_", "-");
    }

    public void loadMinigames() {
        globalMinigameDefaults = null;
        minigameDefaults.clear();
        List<Minigame> minigames = minigameManager.getMinigamesList();
        minigameManager.clearMinigames();

        for (Minigame minigame : minigames) {
            if (minigame instanceof IntermissionMinigame intermissionMinigame && intermissionMinigame.getGameState() == IntermissionMinigame.GameState.RUNNING) {
                intermissionMinigame.end();
            } else if (minigame instanceof PassiveMinigame passiveMinigame) {
                passiveMinigame.stopTasks();
                passiveMinigame.stop();
            }
        }

        File minigamesDirectory = getMinigamesDirectory();
        String[] minigameTypes = minigamesDirectory.list();

        if (minigameTypes == null) {
            return;
        }

        for (String minigameType : minigameTypes) {
            if (!minigameManager.isValidMinigameType(minigameType)) {
                continue;
            }

            File minigameTypeDirectory = new File(minigamesDirectory, minigameType);
            String[] minigameIds = minigameTypeDirectory.list();

            if (minigameIds == null) {
                continue;
            }

            for (String minigameId : minigameIds) {
                File minigameFile = new File(minigameTypeDirectory, minigameId);
                minigameId = minigameId.substring(0, minigameId.length() - 4);

                Minigame minigame = loadFromFile(minigameType, minigameId, minigameFile);
                minigameManager.addMinigame(minigameType, minigameId, minigame);
            }
        }
    }

    private @NotNull Minigame loadFromFile(@NotNull String minigameType, @NotNull String minigameId, @NotNull File file) {
        Minigame minigame = minigameManager.createMinigame(minigameType, minigameId, false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // load options
        for (Map.Entry<String, Option<?>> entry : minigame.getOptions().entrySet()) {
            String optionId = entry.getKey();
            Option<?> option = entry.getValue();
            String path = "options." + formatKey(optionId);

            // check set value
            Object value = config.get(path);
            Object deserialized = (value == null || value.equals(Option.USE_DEFAULT)) ? null : option.deserialize(value);

            if (deserialized != null) {
                option._set(deserialized);
                continue;
            }

            // check minigame defaults
            ConfigurationSection minigameDefaults = getMinigameDefaultsConfig(minigameType);
            if (minigameDefaults == null) continue;

            value = minigameDefaults.get(path);
            deserialized = (value == null) ? null : option.deserialize(value);

            if (deserialized != null) {
                option._setDefault(deserialized);
                continue;
            }

            // check global minigame defaults
            minigameDefaults = getGlobalMinigameDefaultsConfig();
            if (minigameDefaults == null) continue;

            value = globalMinigameDefaults.get(path);
            deserialized = (value == null) ? null : option.deserialize(value);

            if (deserialized != null) {
                option._setDefault(deserialized);
            }
        }

        // load leaderboard settings
        for (Map.Entry<String, Leaderboard> entry : minigame.getLeaderboards().entrySet()) {
            ConfigurationSection leaderboardSection = config.getConfigurationSection("leaderboards." + formatKey(entry.getKey()));
            entry.getValue().loadSettings(leaderboardSection == null ? new YamlConfiguration() : leaderboardSection);
        }

        return minigame;
    }

    private void saveToConfig(@NotNull Map<?, ?> map, @NotNull ConfigurationSection configurationSection) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();

            if (entry.getValue() instanceof Map<?, ?> v) {
                saveToConfig(v, configurationSection.createSection(key));
            } else {
                configurationSection.set(key, entry.getValue());
            }
        }
    }

    public void saveMinigames() {
        File minigamesDirectory = getMinigamesDirectory();

        for (String minigameType : minigameManager.getRegisteredMinigames().keySet()) {
            File minigameTypeDirectory = new File(minigamesDirectory, minigameType);
            deleteDirectory(minigameTypeDirectory);
        }

        for (Map.Entry<String, Map<String, Minigame>> entry : minigameManager.getMinigames().entrySet()) {
            String minigameType = entry.getKey();
            File minigameTypeDirectory = new File(minigamesDirectory, minigameType);

            for (Map.Entry<String, Minigame> e : entry.getValue().entrySet()) {
                String minigameId = e.getKey();
                Minigame minigame = e.getValue();
                File minigameFile = new File(minigameTypeDirectory, minigameId + ".yml");
                saveToFile(minigameFile, minigame);
            }
        }
    }

    private void saveToFile(@NotNull File file, @NotNull Minigame minigame) {
        YamlConfiguration config = new YamlConfiguration();

        // save options
        for (Map.Entry<String, Option<?>> entry : minigame.getOptions().entrySet()) {
            String optionId = entry.getKey();
            Option<?> option = entry.getValue();
            String path = "options." + formatKey(optionId);
            Object value = option.serialize();

            if (value == null) {
                config.set(path, Option.USE_DEFAULT);
            } else {
                saveToConfig(Map.of(path, value), config);
            }
        }

        // save leaderboards
        for (Map.Entry<String, Leaderboard> entry : minigame.getLeaderboards().entrySet()) {
            ConfigurationSection cs = config.createSection("leaderboards." + formatKey(entry.getKey()));
            entry.getValue().saveSettings(cs);
        }

        // save config
        try {
            config.save(file);
        } catch (IOException e) {
            dynamicMinigames.getLogger().severe("Unable to save minigame file: " + file.getName());
            e.printStackTrace();
        }
    }

    private static boolean deleteDirectory(File dir) {
        if (!dir.exists()) return false;

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        return dir.delete();
    }
}
