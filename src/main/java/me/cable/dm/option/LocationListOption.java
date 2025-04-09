package me.cable.dm.option;

import me.cable.dm.option.abs.ListOption;
import me.cable.dm.util.LocationReference;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocationListOption extends ListOption<LocationReference> {

    @Override
    public @NotNull Object listSerialize(@NotNull LocationReference locationReference) {
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
    public @Nullable LocationReference listDeserialize(@NotNull ConfigurationSection configurationSection) {
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
