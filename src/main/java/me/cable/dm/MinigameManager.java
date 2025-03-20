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
        return isValidMinigameType(minigameType)
                && (!minigames.containsKey(minigameType) || !minigames.get(minigameType).containsKey(minigameId));
    }

    public @NotNull Map<String, Supplier<? extends Minigame>> getRegisteredMinigames() {
        return Map.copyOf(registeredMinigames);
    }

    public @NotNull Map<String, Map<String, Minigame>> getMinigames() {
        return Map.copyOf(minigames);
    }

    public @NotNull List<Minigame> getMinigamesList() {
        List<Minigame> list = new ArrayList<>();
        minigames.values().forEach(v -> list.addAll(v.values()));
        return list;
    }

    public @Nullable Minigame getMinigame(@NotNull String minigameType, @NotNull String minigameId) {
        return minigames.containsKey(minigameType) ? minigames.get(minigameType).get(minigameId) : null;
    }

    public void addMinigame(@NotNull String minigameType, @NotNull String minigameId, @NotNull Minigame minigame) {
        if (!isValidMinigameType(minigameType)) {
            throw new IllegalArgumentException("Invalid minigame type: " + minigameType);
        }
        if (!isMinigameIdAvailable(minigameType, minigameId)) {
            throw new IllegalArgumentException("Minigame ID already used: " + minigameId);
        }

        if (minigame instanceof PassiveMinigame passiveMinigame) {
            passiveMinigame.start();
        }

        minigames.computeIfAbsent(minigameType, v -> new HashMap<>()).put(minigameId, minigame);
    }

    public @NotNull Minigame createMinigame(@NotNull String minigameType, @NotNull String minigameId, boolean add) {
        Minigame minigame = registeredMinigames.get(minigameType).get();
        minigame.initialize(minigameType, minigameId);

        if (add) {
            addMinigame(minigameType, minigameId, minigame);
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

    public void clearMinigames() {
        Map<String, Map<String, Minigame>> map = Map.copyOf(minigames);
        minigames.clear(); // clear first so leaderboards are not updated after removal
        map.values().forEach(l -> l.values().forEach(Minigame::removeLeaderboards));
    }
}
