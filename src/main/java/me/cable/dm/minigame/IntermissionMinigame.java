package me.cable.dm.minigame;

import me.cable.dm.DynamicMinigames;
import me.cable.dm.MinigameManager;
import me.cable.dm.option.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class IntermissionMinigame extends Minigame implements Listener {

    private static boolean initialized = false;

    protected final IntegerOption countdownOption;
    protected final IntegerOption minPlayersOption;
    protected final IntegerOption waitingActionsPeriod;
    protected final ActionsOption countdownActionsOption;
    protected final ActionsOption waitingActionsOption;

    protected final RegionOption waitingRegionOption;
    protected final LocationOption endPositionOption;
    protected final ActionsOption startActionsOption;
    protected final ActionsOption endActionsOption;

    private @NotNull GameState gameState = GameState.DISABLED;
    private int countdown;

    public IntermissionMinigame() {
        countdownOption = registerOption("countdown", new IntegerOption());
        minPlayersOption = registerOption("min_players", new IntegerOption());
        waitingActionsPeriod = registerOption("waiting_actions_period", new IntegerOption());
        countdownActionsOption = registerOption("countdown_actions", new ActionsOption());
        waitingActionsOption = registerOption("waiting_actions", new ActionsOption());

        waitingRegionOption = registerOption("waiting_region", new RegionOption());
        endPositionOption = registerOption("end_position", new LocationOption());
        startActionsOption = registerOption("start_actions", new ActionsOption());
        endActionsOption = registerOption("end_actions", new ActionsOption());
    }

    public static void initialize(@NotNull DynamicMinigames dynamicMinigames) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        MinigameManager minigameManager = dynamicMinigames.getMinigameManager();
        initialized = true;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(dynamicMinigames, () -> {
            for (Minigame minigame : minigameManager.getMinigamesList()) {
                if (!(minigame instanceof IntermissionMinigame intermissionMinigame)) {
                    continue;
                }

                GameState gameState = intermissionMinigame.getGameState();

                // check that minigame is enabled and end if disabled while running
                if (gameState == GameState.DISABLED) {
                    if (intermissionMinigame.isEnabled()) {
                        intermissionMinigame.gameState = GameState.WAITING;
                    } else {
                        continue;
                    }
                } else if (!intermissionMinigame.isEnabled()) {
                    if (gameState == GameState.RUNNING) {
                        intermissionMinigame.end();
                    }

                    intermissionMinigame.gameState = GameState.DISABLED;
                    continue;
                }
                if (gameState == GameState.RUNNING) {
                    continue;
                }

                List<Player> waitingPlayers = intermissionMinigame.waitingRegionOption.get().getPlayers();
                gameState = intermissionMinigame.getGameState();

                if (waitingPlayers.size() >= intermissionMinigame.minPlayersOption.get()) {
                    // have enough players: do countdown
                    if (gameState == GameState.WAITING) {
                        intermissionMinigame.gameState = GameState.COUNTDOWN;
                        intermissionMinigame.countdown = intermissionMinigame.countdownOption.get();
                    }

                    if (intermissionMinigame.countdown > 0) {
                        intermissionMinigame.countdownActionsOption.actions()
                                .placeholder("seconds", intermissionMinigame.countdown)
                                .placeholder("s", intermissionMinigame.countdown == 1 ? "" : "s")
                                .run(waitingPlayers);
                    } else {
                        // countdown is up: start minigame
                        intermissionMinigame.gameState = GameState.RUNNING;
                        intermissionMinigame.startActionsOption.actions().run(waitingPlayers);
                        Bukkit.getPluginManager().registerEvents(intermissionMinigame, dynamicMinigames);
                        intermissionMinigame.start(List.copyOf(waitingPlayers));
                    }

                    intermissionMinigame.countdown--;
                } else {
                    // not enough players: go to waiting state
                    if (gameState == GameState.COUNTDOWN) {
                        intermissionMinigame.gameState = GameState.WAITING;
                        intermissionMinigame.countdown = 0;
                    }
                    // run waiting actions periodically
                    if (--intermissionMinigame.countdown <= 0) {
                        intermissionMinigame.waitingActionsOption.actions()
                                .placeholder("min_players", intermissionMinigame.minPlayersOption.get())
                                .placeholder("players", waitingPlayers.size())
                                .run(waitingPlayers);
                        intermissionMinigame.countdown = intermissionMinigame.waitingActionsPeriod.get();
                    }
                }
            }
        }, 0, 20);
    }

    public boolean isEnabled() {
        return true;
    }

    public abstract void start(@NotNull List<Player> players);

    protected final void end(@NotNull List<Player> teleportOnEnd) {
        stopTasks();

        // unregister listeners
        HandlerList.unregisterAll(this);

        // teleport players
        Location location = endPositionOption.get().location();

        if (location.isWorldLoaded()) {
            for (Player player : teleportOnEnd) {
                player.teleport(location);
            }
        }

        // clean up
        cleanup();
        endActionsOption.actions().run(teleportOnEnd);
        gameState = GameState.WAITING;
    }

    public final void end() {
        end(Collections.emptyList());
    }

    public void tick() {
        // empty
    }

    public void cleanup() {
        // empty
    }

    public @NotNull GameState getGameState() {
        return gameState;
    }

    public enum GameState {
        DISABLED,
        WAITING,
        COUNTDOWN,
        RUNNING
    }
}