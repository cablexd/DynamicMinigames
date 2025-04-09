package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockRegion;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlockRegionOption extends Option<BlockRegion> {

    @Override
    public @Nullable Object serialize() {
        BlockRegion blockRegion = _get();
        if (blockRegion == null) return null;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", blockRegion.worldName());
        map.put("x1", blockRegion.x1());
        map.put("y1", blockRegion.y1());
        map.put("z1", blockRegion.z1());
        map.put("x2", blockRegion.x2());
        map.put("y2", blockRegion.y2());
        map.put("z2", blockRegion.z2());
        return map;
    }

    @Override
    public @Nullable BlockRegion deserialize(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        return (world == null) ? null : new BlockRegion(
                world,
                configurationSection.getInt("x1"),
                configurationSection.getInt("y1"),
                configurationSection.getInt("z1"),
                configurationSection.getInt("x2"),
                configurationSection.getInt("y2"),
                configurationSection.getInt("z2")
        );
    }
}