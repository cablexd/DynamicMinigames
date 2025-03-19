package me.cable.dm.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockRegion {

    private final String world;
    private final int x1, y1, z1;
    private final int x2, y2, z2;

    public BlockRegion(@NotNull String world, int x1, int y1, int z1, int x2, int y2, int z2) {
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
                && x >= x1 && x < x2 + 1
                && y >= y1 && y < y2 + 1
                && z >= z1 && z < z2 + 1;
    }

    public boolean contains(@NotNull Block block) {
        World world = block.getWorld();
        return contains(world.getName(), block.getX(), block.getY(), block.getZ());
    }

    public boolean contains(@NotNull Location location) {
        World world = location.getWorld();
        return contains(world == null ? null : world.getName(), location.getX(), location.getY(), location.getZ());
    }

    public @NotNull List<Block> getBlocks() {
        List<Block> list = new ArrayList<>();
        World world = world();

        if (world != null) {
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        list.add(block);
                    }
                }
            }
        }

        return list;
    }

    public void fill(@NotNull Material material) {
        getBlocks().forEach(b -> b.setType(material));
    }

    public @NotNull String worldName() {
        return world;
    }

    public @Nullable World world() {
        return Bukkit.getWorld(world);
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int z1() {
        return z1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public int z2() {
        return z2;
    }
}
