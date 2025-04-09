package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.Region;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegionOption extends Option<Region> {

    @Override
    public @Nullable Object serialize() {
        Region region = _get();
        if (region == null) return null;

        Map<String, Object> map =  new LinkedHashMap<>();
        map.put("world", region.worldName());
        map.put("x1", region.x1());
        map.put("y1", region.y1());
        map.put("z1", region.z1());
        map.put("x2", region.x2());
        map.put("y2", region.y2());
        map.put("z2", region.z2());
        return map;
    }

    @Override
    public @Nullable Region deserialize(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        return (world == null) ? null : new Region(
                world,
                configurationSection.getDouble("x1"),
                configurationSection.getDouble("y1"),
                configurationSection.getDouble("z1"),
                configurationSection.getDouble("x2"),
                configurationSection.getDouble("y2"),
                configurationSection.getDouble("z2")
        );
    }
}