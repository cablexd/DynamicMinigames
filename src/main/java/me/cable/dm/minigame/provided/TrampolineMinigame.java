package me.cable.dm.minigame.provided;

import me.cable.dm.minigame.PassiveMinigame;
import me.cable.dm.option.BlockRegionListOption;
import me.cable.dm.option.IntegerListOption;
import me.cable.dm.option.IntegerOption;
import me.cable.dm.option.abs.AbstractOption;
import me.cable.dm.util.BlockRegion;
import me.cable.dm.util.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TrampolineMinigame extends PassiveMinigame {

    private static final int DEBOUNCE_TICKS = 20;

    private final IntegerListOption velocitiesOption;
    private final BlockRegionListOption blocksOption;

    private final Map<Player, Integer> playerDebounce = new HashMap<>();
    private List<Region> bounceRegions;

    public TrampolineMinigame() {
        velocitiesOption = registerOption("velocities", new IntegerListOption());
        blocksOption = registerOption("blocks", new BlockRegionListOption());
    }

    @Override
    public void onOptionChange(@NotNull String optionId, @NotNull AbstractOption abstractOption) {
        // reset bounce regions when block regions change
        if (optionId.equals("blocks")) {
            bounceRegions = null;
        }
    }

    private void bounce(@NotNull Player player) {
        // check debounce and add to debounce
        if (playerDebounce.containsKey(player)) return;
        playerDebounce.put(player, DEBOUNCE_TICKS);

        // choose velocity
        List<Integer> velocities = velocitiesOption.get();
        int vel;

        if (velocities.isEmpty()) {
            vel = 1;
        } else {
            vel = velocities.get((int) (Math.random() * velocities.size()));
        }

        // fling player
        Vector velocity = player.getVelocity();
        velocity.setY(vel);
        player.setVelocity(velocity);
    }

    @Override
    public void tick() {
        if (bounceRegions == null) {
            bounceRegions = new ArrayList<>();

            // get bounce regions by taking block regions and raising height by 1
            for (BlockRegion blockRegion : blocksOption.get()) {
                Region region = new Region(
                        blockRegion.worldName(),
                        blockRegion.x1(),
                        blockRegion.y1() + 1,
                        blockRegion.z1(),
                        blockRegion.x2(),
                        blockRegion.y2() + 1,
                        blockRegion.z2()
                );
                bounceRegions.add(region);
            }
        }

        // decrease debounce by 1 for each player
        for (Map.Entry<Player, Integer> entry : Map.copyOf(playerDebounce).entrySet()) {
            Player player = entry.getKey();
            int ticks = entry.getValue() - 1;

            if (ticks > 0) {
                playerDebounce.put(player, ticks);
            } else {
                playerDebounce.remove(player);
            }
        }

        // bounce all players in bounce regions
        for (Region region : bounceRegions) {
            World world = region.world();
            if (world == null) continue;

            for (Player player : world.getPlayers()) {
                if (region.contains(player.getLocation())) {
                    bounce(player);
                }
            }
        }
    }
}
