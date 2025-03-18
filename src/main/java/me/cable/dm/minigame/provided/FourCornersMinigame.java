package me.cable.dm.minigame.provided;

import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.option.ActionsOption;
import me.cable.dm.option.BlockOption;
import me.cable.dm.option.IntegerOption;
import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockReference;
import me.cable.dm.util.BlockRegion;
import me.cable.dm.util.PlayerTextInput;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FourCornersMinigame extends IntermissionMinigame {

    private final BlockOption centerBlockOption;
    private final IntegerOption platformSizeOption;
    private final IntegerOption platformGapOption;
    private final IntegerOption fallHeight;
    private final IntegerOption startDelayOption;

    private final PlatformOption[] platformOptions = new PlatformOption[4];

    private final IntegerOption destroyingCountdownOption;
    private final IntegerOption destroyingDurationOption;
    private final IntegerOption replacingDurationOption;
    private final ActionsOption destroyingCountdownActionsOption;
    private final ActionsOption destroyingActionsOption;
    private final ActionsOption replacingActionsOption;

    private final ActionsOption eliminationActionsOption;

    private final BlockRegion[] platformRegions = new BlockRegion[4];
    private List<Player> alivePlayers;

    public FourCornersMinigame() {
        centerBlockOption = registerOption("center_block", new BlockOption());
        platformSizeOption = registerOption("platform_size", new IntegerOption());
        platformGapOption = registerOption("platform_gap", new IntegerOption());
        fallHeight = registerOption("fall_height", new IntegerOption());
        startDelayOption = registerOption("start_delay", new IntegerOption());

        platformOptions[0] = registerOption("platform1", new PlatformOption());
        platformOptions[1] = registerOption("platform2", new PlatformOption());
        platformOptions[2] = registerOption("platform3", new PlatformOption());
        platformOptions[3] = registerOption("platform4", new PlatformOption());

        destroyingCountdownOption = registerOption("destroying_countdown", new IntegerOption());
        destroyingDurationOption = registerOption("destroying_duration", new IntegerOption());
        replacingDurationOption = registerOption("replacing_duration", new IntegerOption());
        destroyingCountdownActionsOption = registerOption("destroying_countdown_actions", new ActionsOption());
        destroyingActionsOption = registerOption("destroying_actions", new ActionsOption());
        replacingActionsOption = registerOption("replacing_actions", new ActionsOption());

        eliminationActionsOption = registerOption("elimination_options", new ActionsOption());
    }

    @Override
    public boolean isEnabled() {
        // check that world is loaded
        return centerBlockOption.get().world() != null;
    }

    private @NotNull String getWorldName() {
        return centerBlockOption.get().worldName();
    }

    private void updatePlatformRegions() {
        BlockReference centerBlock = centerBlockOption.get();
        String world = getWorldName();
        int size = platformSizeOption.get();
        int adding = platformGapOption.get() / 2 + 1;
        int offset = (platformGapOption.get() & 1) == 0 ? 1 : 0; // for even values: shift platforms in certain way

        platformRegions[0] = new BlockRegion(world,
                centerBlock.x() + adding,
                centerBlock.y(),
                centerBlock.z() + adding,
                centerBlock.x() + adding + size - 1,
                centerBlock.y(),
                centerBlock.z() + adding + size - 1);
        platformRegions[1] = new BlockRegion(world,
                centerBlock.x() - adding + offset,
                centerBlock.y(),
                centerBlock.z() + adding,
                centerBlock.x() - adding + offset - size + 1,
                centerBlock.y(),
                centerBlock.z() + adding + size - 1);
        platformRegions[2] = new BlockRegion(world,
                centerBlock.x() - adding + offset,
                centerBlock.y(),
                centerBlock.z() - adding + offset,
                centerBlock.x() - adding + offset - size + 1,
                centerBlock.y(),
                centerBlock.z() - adding + offset - size + 1);
        platformRegions[3] = new BlockRegion(world,
                centerBlock.x() + adding,
                centerBlock.y(),
                centerBlock.z() - adding + offset,
                centerBlock.x() + adding + size - 1,
                centerBlock.y(),
                centerBlock.z() - adding + offset - size + 1);
    }

    private void checkPlayerCount() {
        if (alivePlayers.isEmpty()) {
            end();
        }
    }

    private void placePlatforms() {
        for (int i = 0; i < 4; i++) {
            platformRegions[i].fill(platformOptions[i].get().material);
        }
    }

    @Override
    public void start(@NotNull List<Player> players) {
        alivePlayers = players;
        World world = centerBlockOption.get().world();

        updatePlatformRegions();
        placePlatforms();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            BlockRegion region = platformRegions[i % 4];

            Location location = new Location(world,
                    (region.x1() + region.x2() + 1) / 2.0,
                    region.y1() + 1,
                    (region.z1() + region.z2() + 1) / 2.0);
            player.teleport(location);
        }

        startPlayerDeathChecking();
        startGameLoop();
    }

    private void startPlayerDeathChecking() {
        int deathHeight = centerBlockOption.get().y() + 1 - fallHeight.get();

        runTaskTimer(0, 1, () -> {
            for (Player player : List.copyOf(alivePlayers)) {
                if (player.getLocation().getY() < deathHeight) {
                    alivePlayers.remove(player);
                    player.teleport(endPositionOption.get().location());
                    eliminationActionsOption.actions()
                            .placeholder("player", player.getName())
                            .run(player, alivePlayers);
                }
            }

            checkPlayerCount();
        });
    }

    private void startGameLoop() {
        runTaskTimer(startDelayOption.get() * 20, 20, new BukkitRunnable() {
            int state = 0;
            int countdown;
            boolean firstAction = true;

            @Override
            public void run() {
                switch (state) {
                    case 0 -> { // countdown
                        if (firstAction) {
                            firstAction = false;
                            countdown = destroyingCountdownOption.get();
                        }

                        destroyingCountdownActionsOption.actions()
                                .placeholder("seconds", countdown)
                                .placeholder("s", countdown == 1 ? "" : "s")
                                .run(alivePlayers);

                        if (--countdown <= 0) {
                            state = 1;
                            firstAction = true;
                        }
                    }
                    case 1 -> { // destroy platform
                        if (firstAction) {
                            firstAction = false;
                            countdown = destroyingDurationOption.get();

                            int selectedPlataformI = (int) (Math.random() * 4);
                            Platform platform = platformOptions[selectedPlataformI].get();
                            platformRegions[selectedPlataformI].fill(Material.AIR);

                            destroyingActionsOption.actions()
                                    .placeholder("platform", platform.name)
                                    .run(alivePlayers);
                        }
                        if (--countdown <= 0) {
                            state = 2;
                            firstAction = true;
                        }
                    }
                    case 2 -> { // replace platforms
                        if (firstAction) {
                            firstAction = false;
                            countdown = replacingDurationOption.get();

                            placePlatforms();
                            replacingActionsOption.actions().run(alivePlayers);
                        }
                        if (--countdown <= 0) {
                            state = 0;
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

        for (BlockRegion platformRegion : platformRegions) {
            platformRegion.fill(Material.AIR);
        }
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (alivePlayers.remove(player)) {
            checkPlayerCount();
        }
    }

    private static class Platform {
        @NotNull String name;
        @NotNull Material material;

        public Platform(@NotNull String name, @NotNull Material material) {
            this.name = name;
            this.material = material;
        }
    }

    private static class PlatformOption extends Option<Platform> {

        @Override
        public boolean canSetInGame() {
            return true;
        }

        @Override
        public @Nullable String setInGame(@NotNull Player player, @NotNull String[] args) {
            if (args.length < 1 || !List.of("material", "name").contains(args[0])) {
                return " <material|name>";
            }

            Platform platform;

            if (getRaw() == null) {
                platform = new Platform("New Platform", Material.BLUE_CONCRETE);
                setRaw(platform);
            } else {
                platform = getRaw();
            }

            if (args[0].equals("material")) {
                Material material = player.getInventory().getItemInMainHand().getType();
                platform.material = material;
                player.sendMessage(ChatColor.GREEN + "Set platform material to the material of the item currently being held: " + ChatColor.GOLD + material);
            } else {
                player.sendMessage(ChatColor.GREEN + "Type the new value in chat or type \"cancel\" to cancel:");

                new PlayerTextInput(player, input -> {
                    platform.name = input;
                    player.sendMessage(ChatColor.GREEN + "Set name of platform to: " + ChatColor.GOLD + input);
                    return true;
                }, () -> player.sendMessage(ChatColor.GREEN + "Input cancelled.")).listen();
            }

            return null;
        }

        @Override
        public boolean useConfigurationSection() {
            return true;
        }

        @Override
        public boolean save(@NotNull ConfigurationSection configurationSection) {
            Platform platform = getRaw();
            if (platform == null) return false;

            configurationSection.set("name", platform.name);
            configurationSection.set("material", platform.material.toString());
            return true;
        }

        @Override
        public void load(@NotNull ConfigurationSection configurationSection) {
            setRaw(new Platform(
                    Objects.requireNonNull(configurationSection.getString("name"), "Platform name is null"),
                    Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(
                            configurationSection.getString("material"), "Platform material is null")), "Invalid material")
            ));
        }
    }
}