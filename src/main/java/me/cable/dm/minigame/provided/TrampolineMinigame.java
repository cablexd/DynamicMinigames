package me.cable.dm.minigame.provided;

import me.cable.dm.leaderboard.Leaderboard;
import me.cable.dm.minigame.PassiveMinigame;
import me.cable.dm.option.*;
import me.cable.dm.option.abs.AbstractOption;
import me.cable.dm.util.BlockRegion;
import me.cable.dm.util.Region;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TrampolineMinigame extends PassiveMinigame {

    private static final int DEBOUNCE_TICKS = 10;

    private final DoubleListOption velocitiesOption;
    private final BlockRegionListOption blocksOption;
    private final ActionsOption bounceActionsOption;

    private final Map<Player, Integer> playerDebounce = new HashMap<>();
    private List<Region> bounceRegions;

    public TrampolineMinigame() {
        velocitiesOption = registerOption("velocities", new DoubleListOption());
        blocksOption = registerOption("blocks", new BlockRegionListOption());
        bounceActionsOption = registerOption("bounce_actions", new ActionsOption());

        registerLeaderboard("bounces", v -> Integer.toString(v), (a, b) -> b - a);
    }

    @Override
    public void onOptionChange(@NotNull String optionId, @NotNull AbstractOption abstractOption) {
        // reset bounce regions when block regions change
        if (optionId.equals("blocks")) {
            bounceRegions = null;
        }
    }

    private void increaseBounceScore(@NotNull Player player) {
        setLeaderboardScore("bounces", player.getUniqueId(),
                Objects.requireNonNullElse(getLeaderboardScore("bounces", player.getUniqueId()), 0) + 1);
    }

    private void bounce(@NotNull Player player) {
        // check debounce and add to debounce
        if (playerDebounce.containsKey(player) || player.isSneaking()) return;
        playerDebounce.put(player, DEBOUNCE_TICKS);

        // increase bounce leaderboard score
        increaseBounceScore(player);

        // run actions
        bounceActionsOption.actions().run(player);

        // choose velocity
        List<Double> velocities = velocitiesOption.get();
        double vel;

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
                        blockRegion.x2() + 1,
                        blockRegion.y2() + 2, // add extra one so that the region is at least 1 block tall
                        blockRegion.z2() + 1
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
