package me.cable.dm.minigame.provided;

import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.*;
import me.cable.dm.util.BlockRegion;
import me.cable.dm.util.LocationReference;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HighblockMinigame extends IntermissionMinigame {

    private final IntegerOption gameDurationOption;
    private final IntegerOption placePeriodOption;
    private final IntegerOption placeAmountOption;
    private final StringOption blockMaterialOption;
    private final BlockRegionOption placeRegionOption;
    private final BlockRegionOption clearRegionOption;

    private final ActionsOption gameEndActionsOption;
    private final LocationListOption startPositionsOption;

    private List<Player> players;
    private List<Player> alivePlayers;
    private final Map<UUID, Integer> points = new HashMap<>();

    private int tickCounter = 0;
    private int gameTime = 0;

    public HighblockMinigame() {
        gameDurationOption = registerOption("game_duration", new IntegerOption());
        placePeriodOption = registerOption("place_period", new IntegerOption());
        placeAmountOption = registerOption("place_amount", new IntegerOption());
        blockMaterialOption = registerOption("block_material", new StringOption());
        placeRegionOption = registerOption("place_region", new BlockRegionOption());
        clearRegionOption = registerOption("clear_region", new BlockRegionOption());

        gameEndActionsOption = registerOption("game_end_actions", new ActionsOption());
        startPositionsOption = registerOption("start_positions", new LocationListOption());
    }

    @Override
    public void start(@NotNull List<Player> players) {
        this.players = List.copyOf(players);
        this.alivePlayers = new ArrayList<>(players);

        for (Player player : alivePlayers) {
            points.put(player.getUniqueId(), 0);
        }

        for (int i = 0; i < alivePlayers.size(); i++) {
            LocationReference locationReference = startPositionsOption.get(i % startPositionsOption.size());
            if (locationReference.world() != null) {
                alivePlayers.get(i).teleport(locationReference.location());
            }
        }
    }

    @Override
    public void tick() {
        tickCounter++;
        gameTime++;

        // Place blocks every X ticks
        if (tickCounter >= placePeriodOption.get()) {
            tickCounter = 0;
            placeBlocks();
        }

        // Score players every second (20 ticks)
        if (gameTime % 20 == 0) {
            scorePlayers();
        }

        // End game after duration
        if (gameTime >= gameDurationOption.get() * 20) {
            endGame();
        }
    }

    private void placeBlocks() {
        BlockRegion region = placeRegionOption.get();
        if (region.world() == null) return;

        World world = region.world();
        Random random = new Random();
        Material mat = Material.getMaterial(blockMaterialOption.get());
        if (mat == null) return;

        for (int i = 0; i < placeAmountOption.get(); i++) {
            int x = region.x1() + random.nextInt(region.x2() - region.x1() + 1);
            int z = region.z1() + random.nextInt(region.z2() - region.z1() + 1);
            int y = region.y2(); // Place blocks at the top of the region (y2)

            Block block = new Location(world, x, y, z).getBlock();
            block.setType(mat);
        }
    }

    private void scorePlayers() {
        // Sort players by height (y)
        List<Player> sorted = new ArrayList<>(alivePlayers);
        sorted.sort((a, b) -> Double.compare(b.getLocation().getY(), a.getLocation().getY()));

        for (int i = 0; i < sorted.size(); i++) {
            Player player = sorted.get(i);
            int score = sorted.size() - i;
            points.put(player.getUniqueId(), points.get(player.getUniqueId()) + score);
        }
    }

    private void endGame() {
        // Sort players by points in descending order
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((a, b) -> Integer.compare(points.getOrDefault(b.getUniqueId(), 0), points.getOrDefault(a.getUniqueId(), 0)));

        // Prepare winner placeholders
        String winner1 = sortedPlayers.size() > 0 ? sortedPlayers.get(0).getName() : "N/A";
        int winner1Points = sortedPlayers.size() > 0 ? points.get(sortedPlayers.get(0).getUniqueId()) : 0;

        String winner2 = sortedPlayers.size() > 1 ? sortedPlayers.get(1).getName() : "N/A";
        int winner2Points = sortedPlayers.size() > 1 ? points.get(sortedPlayers.get(1).getUniqueId()) : 0;

        String winner3 = sortedPlayers.size() > 2 ? sortedPlayers.get(2).getName() : "N/A";
        int winner3Points = sortedPlayers.size() > 2 ? points.get(sortedPlayers.get(2).getUniqueId()) : 0;

        // Run the game end actions with placeholders for the top 3 players
        gameEndActionsOption.actions()
                .placeholder("winner_1", winner1)
                .placeholder("winner_1_points", String.valueOf(winner1Points))
                .placeholder("winner_2", winner2)
                .placeholder("winner_2_points", String.valueOf(winner2Points))
                .placeholder("winner_3", winner3)
                .placeholder("winner_3_points", String.valueOf(winner3Points))
                .run(players);

        // Clear region and end the game
        clearRegionOption.get().fill(Material.AIR);
        end(sortedPlayers);
    }

    @Override
    public void cleanup() {
        players = null;
        alivePlayers = null;
        tickCounter = 0;
        gameTime = 0;
        points.clear();
        clearRegionOption.get().fill(Material.AIR);
    }

    @EventHandler
    private void playerQuitEvent(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        alivePlayers.remove(player);
        if (alivePlayers.isEmpty()) {
            endGame();
        }
    }
}
