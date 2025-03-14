package me.cable.dm.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Region {

    private final String world;
    private final double x1, y1, z1;
    private final double x2, y2, z2;

    public Region(@NotNull String world, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.world = world;
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.z2 = Math.max(z1, z2);
    }

    public boolean contains(@Nullable String world, double x, double y, double z) {
        return (world == null || this.world.equals(world))
                && x >= x1 && x < x2
                && y >= y1 && y < y2
                && z >= z1 && z < z2;
    }

    public boolean contains(@NotNull Location location) {
        World world = location.getWorld();
        return contains(world == null ? null : world.getName(), location.getX(), location.getY(), location.getZ());
    }

    public @NotNull List<Player> getPlayers() {
        World w = world();
        return (w == null) ? Collections.emptyList() : w.getPlayers().stream().filter(p -> contains(p.getLocation())).toList();
    }

    public @NotNull String worldName() {
        return world;
    }

    public @Nullable World world() {
        return Bukkit.getWorld(world);
    }

    public double x1() {
        return x1;
    }

    public double y1() {
        return y1;
    }

    public double z1() {
        return z1;
    }

    public double x2() {
        return x2;
    }

    public double y2() {
        return y2;
    }

    public double z2() {
        return z2;
    }
}
