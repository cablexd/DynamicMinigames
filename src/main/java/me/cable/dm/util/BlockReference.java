package me.cable.dm.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockReference {

    private final String world;
    private final int x, y, z;

    public BlockReference(@NotNull String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockReference(@NotNull Block block) {
        this(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public @NotNull BlockReference relative(int x, int y, int z) {
        return new BlockReference(world, this.x + x, this.y + y, this.z + z);
    }

    public @Nullable Block block() {
        World world = world();
        return world == null ? null : world.getBlockAt(x, y, z);
    }

    public @NotNull String worldName() {
        return world;
    }

    public @Nullable World world() {
        return Bukkit.getWorld(world);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }
}
