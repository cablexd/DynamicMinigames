package me.cable.dm.minigame.provided;

import me.cable.actionsapi.Actions;
import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.*;
import me.cable.dm.option.abs.ListOption;
import me.cable.dm.util.LocationReference;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FourCornersMinigame extends IntermissionMinigame {

    private final IntegerOption startDelayOption;
    private final IntegerOption gameCountdownOption;
    private final IntegerOption selectionDurationOption;
    private final IntegerOption intermissionDurationOption;
    private final IntegerOption eliminationHeightOption;

    private final StringListOption eliminationMaterialsOption;
    private final PlatformsOption platformsOptions;
    private final LocationListOption startPositionsOption;

    private final ActionsOption gameCountdownActionsOption;
    private final ActionsOption selectionActionsOption;
    private final ActionsOption intermissionActionsOption;
    private final ActionsOption eliminationActionsOption;

    private List<Player> alivePlayers;
    private int gameState;
    private boolean eliminationMaterialsActive;

    public FourCornersMinigame() {
        startDelayOption = registerOption("start_delay", new IntegerOption());
        gameCountdownOption = registerOption("game_countdown", new IntegerOption());
        selectionDurationOption = registerOption("selection_duration", new IntegerOption());
        intermissionDurationOption = registerOption("intermission_duration", new IntegerOption());
        eliminationHeightOption = registerOption("elimination_height", new IntegerOption());

        eliminationMaterialsOption = registerOption("elimination_materials", new StringListOption());
        platformsOptions = registerOption("platforms", new PlatformsOption());
        startPositionsOption = registerOption("start_positions", new LocationListOption());

        gameCountdownActionsOption = registerOption("game_countdown_actions", new ActionsOption());
        selectionActionsOption = registerOption("selection_actions", new ActionsOption());
        intermissionActionsOption = registerOption("intermission_actions", new ActionsOption());
        eliminationActionsOption = registerOption("elimination_actions", new ActionsOption());
    }

    private void checkPlayerCount() {
        if (alivePlayers.isEmpty()) {
            end();
        }
    }

    @Override
    public void start(@NotNull List<Player> players) {
        alivePlayers = players;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            LocationReference locationReference = startPositionsOption.get(i % 4);
            player.teleport(locationReference.location());
        }

        startPlayerEliminationChecking();
        startGameLoop();
    }

    private void eliminate(@NotNull Player player, boolean actions) {
        if (actions) {
            player.teleport(endPositionOption.get().location());
            eliminationActionsOption.actions()
                    .placeholder("player", player.getName())
                    .run(player, alivePlayers);
        }

        alivePlayers.remove(player);
        checkPlayerCount();
    }

    private boolean isOnEliminationMaterial(@NotNull Player player, @NotNull List<Material> eliminationMaterials) {
        Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        return eliminationMaterials.contains(block.getType());
    }

    private void startPlayerEliminationChecking() {
        List<Material> eliminationMaterials = new ArrayList<>();
        int eliminationHeight = eliminationHeightOption.get();

        for (String s : eliminationMaterialsOption.get()) {
            Material material = Material.getMaterial(s);
            if (material != null) eliminationMaterials.add(material);
        }

        runTaskTimer(0, 1, () -> {
            for (Player player : List.copyOf(alivePlayers)) {
                // if is under elimination height or is on an elimination material during selection state
                if (player.getLocation().getY() < eliminationHeight || (eliminationMaterialsActive && isOnEliminationMaterial(player, eliminationMaterials))) {
                    eliminate(player, true);
                }
            }
        });
    }

    private void startGameLoop() {
        runTaskTimer(startDelayOption.get() * 20, 20, new BukkitRunnable() {
            int countdown;
            Platform selectedPlatform;
            boolean firstAction = true;

            @Override
            public void run() {
                switch (gameState) {
                    case 0 -> { // countdown
                        if (firstAction) {
                            firstAction = false;
                            countdown = gameCountdownOption.get();
                        }

                        gameCountdownActionsOption.actions()
                                .placeholder("s", countdown == 1 ? "" : "s")
                                .placeholder("seconds", countdown)
                                .run(alivePlayers);

                        if (--countdown <= 0) {
                            gameState = 1;
                            firstAction = true;
                        }
                    }
                    case 1 -> { // elimination
                        if (firstAction) {
                            firstAction = false;
                            countdown = selectionDurationOption.get();
                            eliminationMaterialsActive = true;
                            selectedPlatform = platformsOptions.get((int) (Math.random() * platformsOptions.size()));
                        }

                        // run all-platform actions
                        selectionActionsOption.actions()
                                .placeholder("platform", selectedPlatform.name)
                                .placeholder("s", countdown == 1 ? "" : "s")
                                .placeholder("seconds", countdown)
                                .run(alivePlayers);
                        // run platform-specific actions
                        new Actions(selectedPlatform.selectedActions)
                                .placeholder("platform", selectedPlatform.name)
                                .placeholder("s", countdown == 1 ? "" : "s")
                                .placeholder("seconds", countdown)
                                .run(alivePlayers);

                        if (--countdown <= 0) {
                            gameState = (intermissionDurationOption.get() == 0) ? 0 : 2;
                            firstAction = true;
                            selectedPlatform = null;
                        }
                    }
                    case 2 -> {
                        if (firstAction) {
                            firstAction = false;
                            countdown = intermissionDurationOption.get();
                            eliminationMaterialsActive = false;
                        }

                        intermissionActionsOption.actions()
                                .placeholder("s", countdown == 1 ? "" : "s")
                                .placeholder("seconds", countdown)
                                .run(alivePlayers);

                        if (--countdown <= 0) {
                            gameState = 0;
                            firstAction = true;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void cleanup() {
        alivePlayers = null;
        gameState = 0;
        eliminationMaterialsActive = false;
    }

    @EventHandler
    private void playerQuitEvent(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        eliminate(player, false);
    }

    @EventHandler
    private void playerDeathEvent(@NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player) || !alivePlayers.contains(player)) return;

        if (player.getHealth() - e.getFinalDamage() <= 0) {
            e.setCancelled(true);
            player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
            eliminate(player, true);
        }
    }

    private static class Platform {
        @NotNull String name;
        @NotNull List<String> selectedActions;

        public Platform(@NotNull String name, @NotNull List<String> selectedActions) {
            this.name = name;
            this.selectedActions = selectedActions;
        }
    }

    private static class PlatformsOption extends ListOption<Platform> {

        @Override
        public boolean useConfigurationSection() {
            return true;
        }

        @Override
        public void listSave(@NotNull Platform platform, @NotNull ConfigurationSection configurationSection) {
            configurationSection.set("name", platform.name);
            configurationSection.set("selected-actions", platform.selectedActions);
        }

        @Override
        public @Nullable Platform listLoad(@NotNull ConfigurationSection configurationSection) {
            return new Platform(
                    configurationSection.getString("name", "Platform"),
                    configurationSection.getStringList("selected-actions")
            );
        }
    }
}