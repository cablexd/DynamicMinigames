package me.cable.dm.option;

import me.cable.dm.option.abs.ListOption;
import me.cable.dm.util.LocationReference;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocationListOption extends ListOption<LocationReference> {

    @Override
    public boolean useConfigurationSection() {
        return true;
    }

    @Override
    public void listSave(@NotNull LocationReference locationReference, @NotNull ConfigurationSection configurationSection) {
        configurationSection.set("world", locationReference.worldName());
        configurationSection.set("x", locationReference.x());
        configurationSection.set("y", locationReference.y());
        configurationSection.set("z", locationReference.z());
        configurationSection.set("yaw", locationReference.yaw());
        configurationSection.set("pitch", locationReference.pitch());
    }

    @Override
    public @Nullable LocationReference listLoad(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        if (world == null) throw new IllegalStateException("Missing world name");

        return new LocationReference(
                world,
                configurationSection.getDouble("x"),
                configurationSection.getDouble("y"),
                configurationSection.getDouble("z"),
                (float) configurationSection.getDouble("yaw"),
                (float) configurationSection.getDouble("pitch")
        );
    }
}
