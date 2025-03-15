package me.cable.dm;

import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.minigame.Minigame;
import me.cable.dm.minigame.PassiveMinigame;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

    private @NotNull String getOptionKey(@NotNull String optionId) {
        return optionId.replace("_", "-");
    }

    private void saveToFile(@NotNull File file, @NotNull Minigame minigame) {
        YamlConfiguration config = new YamlConfiguration();

        // save options
        for (Map.Entry<String, AbstractOption> entry : minigame.getOptions().entrySet()) {
            String optionId = entry.getKey();
            AbstractOption abstractOption = entry.getValue();
            String path = "options." + getOptionKey(optionId);

            if (abstractOption.useConfigurationSection()) {
                ConfigurationSection cs = config.createSection("options." + optionId);
                boolean saved = abstractOption.save(cs);

                if (!saved) {
                    config.set(path, AbstractOption.USE_DEFAULT);
                }
            } else {
                Object serialized = abstractOption.save();
                config.set(path, serialized == null ? AbstractOption.USE_DEFAULT : serialized);
            }
        }

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

    private @Nullable Minigame loadFromFile(@NotNull String minigameType, @NotNull File file) {
        if (!minigameManager.getRegisteredMinigames().containsKey(minigameType)) {
            return null;
        }

        Minigame minigame = minigameManager.getRegisteredMinigames().get(minigameType).get();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // load options
        for (Map.Entry<String, AbstractOption> entry : minigame.getOptions().entrySet()) {
            String optionId = entry.getKey();
            AbstractOption abstractOption = entry.getValue();
            String path = "options." + getOptionKey(optionId);

            if (abstractOption.useConfigurationSection()) {
                ConfigurationSection cs = config.getConfigurationSection("options." + optionId);

                if (cs != null) {
                    abstractOption.load(cs);
                }
            } else {
                Object value = config.get(path);

                if (value != null && !(value instanceof String s && s.equals(AbstractOption.USE_DEFAULT))) {
                    abstractOption.load(value);
                }
            }
        }

        return minigame;
    }

    public void loadMinigames() {
        List<Minigame> minigames = minigameManager.getAllMinigames();
        minigameManager.getMinigames().clear();

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

        if (minigameTypes != null) {
            for (String minigameType : minigameTypes) {
                File minigameTypeDirectory = new File(minigamesDirectory, minigameType);

                String[] minigameIds = minigameTypeDirectory.list();

                if (minigameIds != null) {
                    for (String minigameId : minigameIds) {
                        File minigameFile = new File(minigameTypeDirectory, minigameId);
                        minigameId = minigameId.substring(0, minigameId.length() - 4);
                        Minigame minigame = loadFromFile(minigameType, minigameFile);

                        if (minigame != null) {
                            if (minigame instanceof PassiveMinigame passiveMinigame) {
                                passiveMinigame.start();
                            }

                            minigameManager.getMinigames().computeIfAbsent(minigameType, v -> new HashMap<>()).put(minigameId, minigame);
                        }
                    }
                }
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
