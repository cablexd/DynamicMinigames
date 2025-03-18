package me.cable.dm.minigame.provided;

import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.*;
import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockRegion;
import me.cable.dm.util.LocationReference;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpleefMinigame extends IntermissionMinigame {

    private final IntegerOption gameCountdownOption;
    private final IntegerOption eliminationHeightOption;

    private final LocationListOption startPositionsOption;
    private final StringOption worldOption;
    private final SpleefBlocksOption blocksOption;

    private final ActionsOption gameCountdownActionsOption;
    private final ActionsOption gameStartActionsOption;
    private final ActionsOption eliminationActionsOption;
    private final ActionsOption winActionsOption;

    private boolean started;
    private List<Player> players;
    private List<Player> alivePlayers;

    public SpleefMinigame() {
        gameCountdownOption = registerOption("game_countdown", new IntegerOption());
        eliminationHeightOption = registerOption("elimination_height", new IntegerOption());

        startPositionsOption = registerOption("start_positions", new LocationListOption());
        worldOption = registerOption("world", new StringOption());
        blocksOption = registerOption("blocks", new SpleefBlocksOption());

        gameCountdownActionsOption = registerOption("game_countdown_actions", new ActionsOption());
        gameStartActionsOption = registerOption("game_start_actions", new ActionsOption());
        eliminationActionsOption = registerOption("elimination_actions", new ActionsOption());
        winActionsOption = registerOption("win_actions", new ActionsOption());
    }

    private void checkPlayerCount() {
        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                winActionsOption.actions().run(alivePlayers.getFirst(), players);
            }

            end(alivePlayers);
        }
    }

    private @NotNull Map<BlockRegion, Material> getBlockRegions() {
        Map<BlockRegion, Material> map = new HashMap<>();

        for (Map.Entry<String, Material> entry : blocksOption.get().entrySet()) {
            String[] parts = entry.getKey().split(",");

            try {
                BlockRegion blockRegion = new BlockRegion(
                        worldOption.get(),
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5])
                );
                map.put(blockRegion, entry.getValue());
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                // empty
            }
        }

        return map;
    }

    private void fillBlocks() {
        for (Map.Entry<BlockRegion, Material> entry : getBlockRegions().entrySet()) {
            entry.getKey().fill(entry.getValue());
        }
    }

    private void startGame() {
        started = true;
        gameStartActionsOption.actions().run(alivePlayers);
    }

    @Override
    public void start(@NotNull List<Player> players) {
        this.players = List.copyOf(players);
        alivePlayers = players;

        fillBlocks();

        // teleport players to start positions
        for (int i = 0; i < alivePlayers.size(); i++) {
            LocationReference locationReference = startPositionsOption.get(i % startPositionsOption.size());

            if (locationReference.world() != null) {
                alivePlayers.get(i).teleport(locationReference.location());
            }
        }

        runTaskTimer(0, 20, new BukkitRunnable() {
            int countdown = gameCountdownOption.get();

            @Override
            public void run() {
                if (countdown <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                gameCountdownActionsOption.actions()
                        .placeholder("s", countdown == 1 ? "" : "s")
                        .placeholder("seconds", countdown)
                        .run(alivePlayers);
                countdown--;
            }
        });
    }

    @Override
    public void tick() {
        for (Player player : List.copyOf(alivePlayers)) {
            if (player.getLocation().getY() < eliminationHeightOption.get()) {
                // eliminate player
                player.teleport(endPositionOption.get().location());
                eliminationActionsOption.actions().run(player, players);
                alivePlayers.remove(player);
                checkPlayerCount();
            }
        }
    }

    @Override
    public void cleanup() {
        started = false;
        players = null;
        alivePlayers = null;

        fillBlocks();
    }

    @EventHandler
    private void blockBreakEvent(@NotNull BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (!started && alivePlayers.contains(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void playerMoveEvent(@NotNull PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (started || !alivePlayers.contains(player)) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void playerQuitEvent(@NotNull PlayerQuitEvent e) {
        alivePlayers.remove(e.getPlayer());
        checkPlayerCount();
    }

    private static class SpleefBlocksOption extends Option<Map<String, Material>> {

        @Override
        public boolean useConfigurationSection() {
            return true;
        }

        @Override
        public boolean save(@NotNull ConfigurationSection configurationSection) {
            if (getRaw() == null) return false;

            for (Map.Entry<String, Material> entry : getRaw().entrySet()) {
                configurationSection.set(entry.getKey(), entry.getValue().toString());
            }

            return true;
        }

        @Override
        public void load(@NotNull ConfigurationSection configurationSection) {
            Map<String, Material> map = new HashMap<>();

            for (String key : configurationSection.getKeys(false)) {
                String matName = configurationSection.getString(key);
                if (matName == null) continue;

                Material material = Material.getMaterial(matName);

                if (material != null) {
                    map.put(key, material);
                }
            }

            setRaw(map);
        }
    }
}
