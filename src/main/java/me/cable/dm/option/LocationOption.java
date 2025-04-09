package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.LocationReference;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public @Nullable Object serialize() {
        LocationReference locationReference = _get();
        if (locationReference == null) return null;

        Map<String, Object> map =  new LinkedHashMap<>();
        map.put("world", locationReference.worldName());
        map.put("x", locationReference.x());
        map.put("y", locationReference.y());
        map.put("z", locationReference.z());
        map.put("yaw", locationReference.yaw());
        map.put("pitch", locationReference.pitch());
        return map;


    }

    @Override
    public @Nullable LocationReference deserialize(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        return (world == null) ? null : new LocationReference(
                world,
                configurationSection.getDouble("x"),
                configurationSection.getDouble("y"),
                configurationSection.getDouble("z"),
                (float) configurationSection.getDouble("yaw"),
                (float) configurationSection.getDouble("pitch")
        );
    }
}
