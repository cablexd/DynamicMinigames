package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigurationSectionOption extends Option<ConfigurationSection> {

    @Override
    public @Nullable Object serialize() {
        return _get();
    }

    @Override
    public @Nullable ConfigurationSection deserialize(@NotNull ConfigurationSection configurationSection) {
        return configurationSection;
    }
}
