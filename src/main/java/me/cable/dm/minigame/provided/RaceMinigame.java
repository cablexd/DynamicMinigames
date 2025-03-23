package me.cable.dm.minigame.provided;

import me.cable.actionsapi.Actions;
import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.*;
import me.cable.dm.util.Region;
import me.cable.dm.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaceMinigame extends IntermissionMinigame {

    /*
        TODO:
        add finish position
        max time limit
     */

    private static final String LEADERBOARD_FASTEST_TIMES = "fastest_times";
    private static final String LEADERBOARD_FASTEST_LAP_TIMES = "fastest_lap_times";

    private final IntegerOption gameCountdownOption;
    private final IntegerOption lapsOption;
    private final LocationOption startPositionOption;

    private final RegionOption finishLineRegionOption;
    private final RegionOption courseRegion1Option;
    private final RegionOption courseRegion2Option;
    private final StringListOption teleportRegionsOption;

    private final ActionsOption gameCountdownActionsOption;
    private final ActionsOption gameStartActions;
    private final ActionsOption lapActionsOption;
    private final ActionsOption finishActionsOption;

    private List<Player> players;
    private final Map<Player, Integer> lapsCompleted = new HashMap<>();
    private final Map<Player, List<Long>> lapTimestamps = new HashMap<>();
    private final Map<Player, Integer> courseRegion1 = new HashMap<>();
    private final Map<Player, Integer> courseRegion2 = new HashMap<>();

    public RaceMinigame() {
        gameCountdownOption = registerOption("game_countdown", new IntegerOption());
        lapsOption = registerOption("laps", new IntegerOption());
        startPositionOption = registerOption("start_position", new LocationOption());

        finishLineRegionOption = registerOption("finish_line_region", new RegionOption());
        courseRegion1Option = registerOption("course_region_1", new RegionOption());
        courseRegion2Option = registerOption("course_region_2", new RegionOption());
        teleportRegionsOption = registerOption("teleport_regions", new StringListOption());

        gameCountdownActionsOption = registerOption("game_countdown_actions", new ActionsOption());
        gameStartActions = registerOption("game_start_actions", new ActionsOption());
        lapActionsOption = registerOption("lap_actions", new ActionsOption());
        finishActionsOption = registerOption("finish_actions", new ActionsOption());

        registerLeaderboard(LEADERBOARD_FASTEST_TIMES, Utils::formatMillis, Long::compare);
        registerLeaderboard(LEADERBOARD_FASTEST_LAP_TIMES, Utils::formatMillis, Long::compare);
    }

    private void checkPlayerCount() {
        if (players.isEmpty()) {
            end();
        }
    }

    @Override
    public void start(@NotNull List<Player> players) {
        this.players = players;

        // teleport players to start position
        for (Player player : players) {
            player.teleport(startPositionOption.get().location());
        }

        runTaskTimer(0, 20, new BukkitRunnable() {
            int countdown = gameCountdownOption.get();

            @Override
            public void run() {
                if (countdown <= 0) {
                    long startTime = System.currentTimeMillis();
                    cancel();
                    gameStartActions.actions().run(players);

                    // set first lap timestamp for all players
                    for (Player player : players) {
                        List<Long> l = new ArrayList<>();
                        l.add(startTime);
                        lapTimestamps.put(player, l);
                    }

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

    private void saveLeaderboardData(@NotNull Player player, long totalTime, long quickestLapTime) {
        Long currentQuickestTime = getLeaderboardScore(LEADERBOARD_FASTEST_TIMES, player.getUniqueId());
        Long currentQuickestLapTime = getLeaderboardScore(LEADERBOARD_FASTEST_LAP_TIMES, player.getUniqueId());

        if (currentQuickestTime == null || totalTime < currentQuickestTime) {
            setLeaderboardScore(LEADERBOARD_FASTEST_TIMES, player.getUniqueId(), totalTime);
        }
        if (currentQuickestLapTime == null || quickestLapTime < currentQuickestLapTime) {
            setLeaderboardScore(LEADERBOARD_FASTEST_LAP_TIMES, player.getUniqueId(), quickestLapTime);
        }
    }

    private void onRaceComplete(@NotNull Player player) {
        players.remove(player);
        player.teleport(endPositionOption.get().location());

        // handle lap times: add lap time placeholders and get quickest lap time
        List<Long> playerLapTimestamps = lapTimestamps.get(player);
        Actions finishActions = finishActionsOption.actions();
        long totalTime = playerLapTimestamps.get(lapsOption.get()) - playerLapTimestamps.getFirst();
        long fastestLapTime = Long.MAX_VALUE;

        for (int i = 0; i < lapsOption.get(); i++) {
            long lapTime = playerLapTimestamps.get(i + 1) - playerLapTimestamps.get(i);
            fastestLapTime = Math.min(fastestLapTime, lapTime);
            finishActions.placeholder("lap_time_" + (i + 1), Utils.formatMillis(lapTime));
        }

        finishActions
                .placeholder("average_lap_time", Utils.formatMillis(totalTime / lapsOption.get()))
                .placeholder("fastest_lap_time", Utils.formatMillis(fastestLapTime))
                .placeholder("total_time", Utils.formatMillis(totalTime))
                .run(player, players);

        saveLeaderboardData(player, totalTime, fastestLapTime);
        checkPlayerCount();
    }

    private void onLapComplete(@NotNull Player player) {
        int totalLaps = lapsCompleted.getOrDefault(player, 0) + 1;
        lapsCompleted.put(player, totalLaps);

        List<Long> playerLapTimestamps = lapTimestamps.get(player);
        playerLapTimestamps.add(System.currentTimeMillis());

        if (totalLaps >= lapsOption.get()) {
            onRaceComplete(player);
        } else {
            lapActionsOption.actions()
                    .placeholder("current_lap", totalLaps + 1)
                    .placeholder("laps", lapsOption.get())
                    .placeholder("previous_lap", totalLaps)
                    .placeholder("lap_time", Utils.formatMillis(playerLapTimestamps.get(totalLaps) - playerLapTimestamps.get(totalLaps - 1)))
                    .placeholder("total_time", Utils.formatMillis(playerLapTimestamps.get(totalLaps) - playerLapTimestamps.getFirst()))
                    .run(player, players);
        }
    }

    private @NotNull Map<Region, Location> getTeleportRegions() {
        Map<Region, Location> map = new HashMap<>();

        for (String s : teleportRegionsOption.get()) {
            String[] parts = s.split(":");
            String[] regionParts = parts[0].split(",");
            String[] locParts = parts[1].split(",");

            Region region = new Region(
                    regionParts[0],
                    Double.parseDouble(regionParts[1]),
                    Double.parseDouble(regionParts[2]),
                    Double.parseDouble(regionParts[3]),
                    Double.parseDouble(regionParts[4]),
                    Double.parseDouble(regionParts[5]),
                    Double.parseDouble(regionParts[6])
            );

            World locWorld = Bukkit.getWorld(locParts[0]);
            if (locWorld == null) continue;

            Location loc = new Location(
                    locWorld,
                    Double.parseDouble(locParts[1]),
                    Double.parseDouble(locParts[2]),
                    Double.parseDouble(locParts[3]),
                    Float.parseFloat(locParts[4]),
                    Float.parseFloat(locParts[5])
            );
            map.put(region, loc);
        }

        return map;
    }

    @Override
    public void tick() {
        Map<Region, Location> teleportRegions = getTeleportRegions();

        for (Player player : List.copyOf(players)) {
            Location loc = player.getLocation();

            // teleport regions
            for (Map.Entry<Region, Location> entry : teleportRegions.entrySet()) {
                if (entry.getKey().contains(loc)) {
                    player.teleport(entry.getValue());
                    break;
                }
            }

            // manage race regions to ensure player completes lap
            if (finishLineRegionOption.get().contains(loc)) {
                if (courseRegion1.getOrDefault(player, 0) == 1 && courseRegion2.getOrDefault(player, 0) == 1) {
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
        lapsCompleted.clear();
        courseRegion1.clear();
        courseRegion2.clear();
    }

    @EventHandler
    private void playerQuitEvent(@NotNull PlayerQuitEvent e) {
        players.remove(e.getPlayer());
        checkPlayerCount();
    }
}
