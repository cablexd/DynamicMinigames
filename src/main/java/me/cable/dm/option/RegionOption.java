package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.Region;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionOption extends Option<Region> {

    @Override
    public boolean useConfigurationSection() {
        return true;
    }

    @Override
    public boolean save(@NotNull ConfigurationSection configurationSection) {
        Region region = getRaw();
        if (region == null) return false;

        configurationSection.set("world", region.worldName());
        configurationSection.set("x1", region.x1());
        configurationSection.set("y1", region.y1());
        configurationSection.set("z1", region.z1());
        configurationSection.set("x2", region.x2());
        configurationSection.set("y2", region.y2());
        configurationSection.set("z2", region.z2());
        return true;
    }

    @Override
    public void load(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        if (world == null) throw new IllegalStateException("Missing world name");

        setRaw(new Region(
                world,
                configurationSection.getDouble("x1"),
                configurationSection.getDouble("y1"),
                configurationSection.getDouble("z1"),
                configurationSection.getDouble("x2"),
                configurationSection.getDouble("y2"),
                configurationSection.getDouble("z2")
        ));
    }
}