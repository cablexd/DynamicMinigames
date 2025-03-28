package me.cable.dm;

import me.cable.dm.leaderboard.Leaderboard;
import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.minigame.Minigame;
import me.cable.dm.minigame.PassiveMinigame;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MinigameSerializer {

    private final MinigameManager minigameManager;

    public MinigameSerializer(@NotNull DynamicMinigames dynamicMinigames) {
        minigameManager = dynamicMinigames.getMinigameManager();
    }

    private @NotNull File getMinigamesDirectory() {
        return new File(JavaPlugin.getPlugin(DynamicMinigames.class).getDataFolder(), "Minigames");
    }

    private @NotNull String formatKey(@NotNull String key) {
        return key.replace("_", "-");
    }

    private void saveToFile(@NotNull File file, @NotNull Minigame minigame) {
        YamlConfiguration config = new YamlConfiguration();

        // save options
        for (Map.Entry<String, AbstractOption> entry : minigame.getOptions().entrySet()) {
            String optionId = entry.getKey();
            AbstractOption abstractOption = entry.getValue();
            String path = "options." + formatKey(optionId);

            if (abstractOption.useConfigurationSection()) {
                ConfigurationSection cs = config.createSection(path);
                boolean saved = abstractOption.save(cs);

                if (!saved) {
                    config.set(path, AbstractOption.USE_DEFAULT);
                }
            } else {
                Object serialized = abstractOption.save();
                config.set(path, serialized == null ? AbstractOption.USE_DEFAULT : serialized);
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
            throw new RuntimeException(e);
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

    private @NotNull Minigame loadFromFile(@NotNull String minigameType, @NotNull String minigameId, @NotNull File file) {
        Minigame minigame = minigameManager.createMinigame(minigameType, minigameId, false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // load options
        for (Map.Entry<String, AbstractOption> entry : minigame.getOptions().entrySet()) {
            String optionId = entry.getKey();
            AbstractOption abstractOption = entry.getValue();
            String path = "options." + formatKey(optionId);

            if (abstractOption.useConfigurationSection()) {
                ConfigurationSection cs = config.getConfigurationSection(path);

                if (cs != null) {
                    try {
                        abstractOption.load(cs);
                    } catch (Exception e) {
                        JavaPlugin.getProvidingPlugin(DynamicMinigames.class).getLogger().warning(
                                "Error while loading option " + optionId + " for minigame " + minigameId + " of type " + minigameType);
                        e.printStackTrace();
                    }
                }
            } else {
                Object value = config.get(path);

                if (value != null && !(value instanceof String s && s.equals(AbstractOption.USE_DEFAULT))) {
                    try {
                        abstractOption.load(value);
                    } catch (Exception e) {
                        JavaPlugin.getProvidingPlugin(DynamicMinigames.class).getLogger().warning(
                                "Error while loading option " + optionId + " for minigame " + minigameId + " of type " + minigameType);
                        e.printStackTrace();
                    }
                }
            }
        }

        // load leaderboard settings
        for (Map.Entry<String, Leaderboard> entry : minigame.getLeaderboards().entrySet()) {
            ConfigurationSection leaderboardSection = config.getConfigurationSection("leaderboards." + formatKey(entry.getKey()));
            entry.getValue().loadSettings(leaderboardSection == null ? new YamlConfiguration() : leaderboardSection);
        }

        return minigame;
    }

    public void loadMinigames() {
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
