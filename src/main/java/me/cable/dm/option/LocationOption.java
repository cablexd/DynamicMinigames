package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockReference;
import me.cable.dm.util.LocationReference;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocationOption extends Option<LocationReference> {

    @Override
    public boolean canSetInGame() {
        return true;
    }

    @Override
    public @Nullable String setInGame(@NotNull Player player, @NotNull String[] args) {
        Location location = player.getLocation();
        set(new LocationReference(location));
        player.sendMessage(ChatColor.GREEN + "Set value to your current location: "
                + ChatColor.GOLD + location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " + location.getYaw() + ", " + location.getPitch());
        return null;
    }

    @Override
    public boolean useConfigurationSection() {
        return true;
    }

    @Override
    public boolean save(@NotNull ConfigurationSection configurationSection) {
        LocationReference locationReference = getRaw();
        if (locationReference == null) return false;

        configurationSection.set("world", locationReference.worldName());
        configurationSection.set("x", locationReference.x());
        configurationSection.set("y", locationReference.y());
        configurationSection.set("z", locationReference.z());
        configurationSection.set("yaw", locationReference.yaw());
        configurationSection.set("pitch", locationReference.pitch());
        return true;
    }

    @Override
    public void load(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        if (world == null) throw new IllegalStateException("Missing world name");

        setRaw(new LocationReference(
                world,
                configurationSection.getDouble("x"),
                configurationSection.getDouble("y"),
                configurationSection.getDouble("z"),
                (float) configurationSection.getDouble("yaw"),
                (float) configurationSection.getDouble("pitch")
        ));
    }
}
