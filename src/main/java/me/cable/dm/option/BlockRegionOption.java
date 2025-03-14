package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockRegion;
import me.cable.dm.util.Region;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class BlockRegionOption extends Option<BlockRegion> {

    @Override
    public boolean useConfigurationSection() {
        return true;
    }

    @Override
    public boolean save(@NotNull ConfigurationSection configurationSection) {
        BlockRegion blockRegion = getRaw();
        if (blockRegion == null) return false;

        configurationSection.set("world", blockRegion.worldName());
        configurationSection.set("x1", blockRegion.x1());
        configurationSection.set("y1", blockRegion.y1());
        configurationSection.set("z1", blockRegion.z1());
        configurationSection.set("x2", blockRegion.x2());
        configurationSection.set("y2", blockRegion.y2());
        configurationSection.set("z2", blockRegion.z2());
        return true;
    }

    @Override
    public void load(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        if (world == null) throw new IllegalStateException("Missing world name");

        setRaw(new BlockRegion(
                world,
                configurationSection.getInt("x1"),
                configurationSection.getInt("y1"),
                configurationSection.getInt("z1"),
                configurationSection.getInt("x2"),
                configurationSection.getInt("y2"),
                configurationSection.getInt("z2")
        ));
    }
}