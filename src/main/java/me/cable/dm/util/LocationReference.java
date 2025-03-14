package me.cable.dm.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LocationReference {

    private final String world;
    private final double x, y, z;
    private final float yaw, pitch;

    public LocationReference(@NotNull String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LocationReference(@NotNull Location location) {
        this(Objects.requireNonNull(location.getWorld()).getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public @NotNull Location location() {
        return new Location(world(), x, y, z, yaw, pitch);
    }

    public @NotNull String worldName() {
        return world;
    }

    public @Nullable World world() {
        return Bukkit.getWorld(world);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }
}
