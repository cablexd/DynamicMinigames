package me.cable.dm;

import me.cable.dm.minigame.Minigame;
import me.cable.dm.minigame.PassiveMinigame;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MinigameManager {

    private static final Map<String, Supplier<? extends Minigame>> registeredMinigames = new HashMap<>();
    private final Map<String, Map<String, Minigame>> minigames = new HashMap<>(); // <type, <id, minigame>>

    public static void registerMinigame(@NotNull String minigameTypeId, @NotNull Supplier<Minigame> supplier) {
        if (registeredMinigames.containsKey(minigameTypeId)) {
            throw new IllegalStateException("Tried to register minigame with type \"" + minigameTypeId + "\" but that type already exists");
        }

        JavaPlugin.getPlugin(DynamicMinigames.class).getLogger().info("Minigame type \"" + minigameTypeId + "\" has been registered");
        registeredMinigames.put(minigameTypeId, supplier);
    }

    public boolean isValidMinigameType(@NotNull String minigameType) {
        return registeredMinigames.containsKey(minigameType);
    }

    public boolean isMinigameIdAvailable(@NotNull String minigameType, @NotNull String minigameId) {
        return isValidMinigameType(minigameType) && !minigames.computeIfAbsent(minigameType, v -> new HashMap<>()).containsKey(minigameId);
    }

    public @NotNull Map<String, Map<String, Minigame>> getMinigames() {
        return minigames;
    }

    public @NotNull List<Minigame> getAllMinigames() {
        List<Minigame> list = new ArrayList<>();
        minigames.forEach((a, b) -> list.addAll(b.values()));
        return list;
    }

    public @NotNull Map<String, List<String>> getCreatedMinigames() {
        Map<String, List<String>> map = new HashMap<>();

        for (Map.Entry<String, Map<String, Minigame>> entry : minigames.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                map.put(entry.getKey(), new ArrayList<>(entry.getValue().keySet()));
            }
        }

        return map;
    }

    public @Nullable Minigame getMinigame(@NotNull String minigameType, @NotNull String minigameId) {
        Map<String, Minigame> typedMinigames = minigames.get(minigameType);
        return typedMinigames == null ? null : typedMinigames.get(minigameId);
    }

    public @NotNull Map<String, Supplier<? extends Minigame>> getRegisteredMinigames() {
        return registeredMinigames;
    }

    public @Nullable Minigame createMinigame(@NotNull String minigameType, @NotNull String minigameId) {
        if (!isValidMinigameType(minigameType)) {
            throw new IllegalArgumentException("Invalid minigame type: " + minigameType);
        }
        if (!isMinigameIdAvailable(minigameType, minigameId)) {
            throw new IllegalArgumentException("Minigame ID already used: " + minigameId);
        }

        Minigame minigame = registeredMinigames.get(minigameType).get();
        minigames.computeIfAbsent(minigameType, v -> new HashMap<>()).put(minigameId, minigame);

        if (minigame instanceof PassiveMinigame passiveMinigame) {
            passiveMinigame.start();
        }

        return minigame;
    }

    public void removeMinigame(@NotNull String minigameType, @NotNull String minigameId) {
        if (!isValidMinigameType(minigameType)) {
            throw new IllegalArgumentException("Invalid minigame type: " + minigameType);
        }
        if (isMinigameIdAvailable(minigameType, minigameId)) {
            throw new IllegalArgumentException("Minigame ID does not exist: " + minigameId);
        }

        minigames.get(minigameType).remove(minigameId);
    }
}
