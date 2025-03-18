package me.cable.dm.minigame.provided;

import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaceMinigame extends IntermissionMinigame {

    private final IntegerOption gameCountdownOption;
    private final IntegerOption startDelayOption;
    private final IntegerOption lapsOption;
    private final LocationOption startPositionOption;

    private final RegionOption finishLineRegionOption;
    private final RegionOption courseRegion1Option;
    private final RegionOption courseRegion2Option;

    private final ActionsOption gameCountdownActionsOption;
    private final ActionsOption raceStartActionsOption;
    private final ActionsOption lapActionsOption;
    private final ActionsOption finishActionsOption;

    private List<Player> players;
    private final Map<Player, Integer> lapsCompleted = new HashMap<>();
    private final Map<Player, Integer> courseRegion1 = new HashMap<>();
    private final Map<Player, Integer> courseRegion2 = new HashMap<>();

    public RaceMinigame() {
        gameCountdownOption = registerOption("game_countdown", new IntegerOption());
        startDelayOption = registerOption("start_delay", new IntegerOption());
        lapsOption = registerOption("laps", new IntegerOption());
        startPositionOption = registerOption("start_position", new LocationOption());

        finishLineRegionOption = registerOption("finish_line_region", new RegionOption());
        courseRegion1Option = registerOption("course_region_1", new RegionOption());
        courseRegion2Option = registerOption("course_region_2", new RegionOption());

        gameCountdownActionsOption = registerOption("game_countdown_actions", new ActionsOption());
        raceStartActionsOption = registerOption("race_start_actions", new ActionsOption());
        lapActionsOption = registerOption("lap_actions", new ActionsOption());
        finishActionsOption = registerOption("finish_actions", new ActionsOption());
    }

    @Override
    public boolean isEnabled() {
        // check that world is loaded
        return startPositionOption.get().world() != null;
    }

    private void checkPlayerCount() {
        if (players.isEmpty()) {
            end();
        }
    }

    private void resetCourseRegions() {
        courseRegion1.clear();
        courseRegion2.clear();
    }

    private void resetValues() {
        lapsCompleted.clear();
        resetCourseRegions();
    }

    @Override
    public void start(@NotNull List<Player> players) {
        this.players = players;

        // teleport players to start position
        for (Player player : players) {
            player.teleport(startPositionOption.get().location());
        }

        runTaskTimer(startDelayOption.get() * 20, 20, new BukkitRunnable() {
            int countdown = gameCountdownOption.get();

            @Override
            public void run() {
                if (countdown <= 0) {
                    cancel();
                    raceStartActionsOption.actions().run(players);
                    return;
                }

                gameCountdownActionsOption.actions()
                        .placeholder("seconds", countdown)
                        .placeholder("s", countdown == 1 ? "" : "s")
                        .run(players);
                countdown--;
            }
        });
    }

    private void onLapsComplete(@NotNull Player player) {
        players.remove(player);
        player.teleport(endPositionOption.get().location());
        finishActionsOption.actions().run(player, players);
        checkPlayerCount();
    }

    private void onLapComplete(@NotNull Player player) {
        int laps = lapsCompleted.getOrDefault(player, 0) + 1;
        lapsCompleted.put(player, laps);

        if (laps >= lapsOption.get()) {
            onLapsComplete(player);
        } else {
            lapActionsOption.actions()
                    .placeholder("lap", laps + 1)
                    .placeholder("laps", lapsOption.get())
                    .placeholder("previous_lap", laps)
                    .run(player, players);
        }
    }

    @Override
    public void tick() {
        for (Player player : List.copyOf(players)) {
            Location loc = player.getLocation();

            if (finishLineRegionOption.get().contains(loc)) {
                if (courseRegion1.getOrDefault(player, 0) == 1 && courseRegion1.getOrDefault(player, 0) == 1) {
                    onLapComplete(player);
                }

                // reset course region values
                courseRegion1.put(player, 0);
                courseRegion2.put(player, 0);
            } else if (courseRegion1Option.get().contains(loc)) {
                /*
                    Explanation:
                    if region 2 is locked (-1): region 1 becomes 0
                    if 0 (unvisited), region 1 becomes 1 (visited)
                    if 1 (visited), region 2 becomes 2 (visited twice)
                 */
                courseRegion1.put(player, courseRegion2.getOrDefault(player, 0) + 1);
            } else if (courseRegion2Option.get().contains(loc)) {
                int v = courseRegion1.getOrDefault(player, 0);

                if (v == 0) {
                    courseRegion2.put(player, -1); // lock
                } else { // v > 0
                    courseRegion2.put(player, 1);
                    courseRegion1.put(player, 1); // in case value is 2
                }
            }
        }
    }

    @Override
    public void cleanup() {
        players = null;
        resetValues();
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        players.remove(e.getPlayer());
        checkPlayerCount();
    }
}
