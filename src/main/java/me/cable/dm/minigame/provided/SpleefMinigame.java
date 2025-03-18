package me.cable.dm.minigame.provided;

import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.ActionsOption;
import me.cable.dm.option.IntegerOption;
import me.cable.dm.option.LocationListOption;
import me.cable.dm.util.LocationReference;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpleefMinigame extends IntermissionMinigame {

    private final IntegerOption gameCountdownOption;
    private final LocationListOption startPositions;
    private final ActionsOption gameCountdownActionsOption;
    private final ActionsOption gameStartActionsOption;

    private boolean started;
    private List<Player> players;

    public SpleefMinigame() {
        gameCountdownOption = registerOption("game_countdown", new IntegerOption());
        startPositions = registerOption("start_positions", new LocationListOption());
        gameCountdownActionsOption = registerOption("game_countdown_actions", new ActionsOption());
        gameStartActionsOption = registerOption("game_start_actions", new ActionsOption());
    }

    private void checkPlayerCount() {
        if (players.size() <= 1) {
            end(players);
        }
    }

    private void startGame() {
        started = true;
        gameStartActionsOption.actions().run(players);
    }

    @Override
    public void start(@NotNull List<Player> players) {
        this.players = players;

        // teleport players to start positions
        for (int i = 0; i < players.size(); i++) {
            LocationReference locationReference = startPositions.get(i % startPositions.size());

            if (locationReference.world() != null) {
                players.get(i).teleport(locationReference.location());
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
                        .run(players);
                countdown--;
            }
        });
    }

    @Override
    public void cleanup() {
        started = false;
        players = null;
    }

    @EventHandler
    private void onPlayerMove(@NotNull PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (!started && players.contains(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        players.remove(e.getPlayer());
        checkPlayerCount();
    }
}
