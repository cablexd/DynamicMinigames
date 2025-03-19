package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ConfigurationSectionOption extends Option<ConfigurationSection> {

    @Override
    public boolean save(@NotNull ConfigurationSection configurationSection) {
        ConfigurationSection cs = getRaw();
        if (cs == null) return false;

        for (String key : cs.getKeys(false)) {
            configurationSection.set(key, cs.get(key));
        }

        return true;
    }

    @Override
    public void load(@NotNull ConfigurationSection configurationSection) {
        setRaw(configurationSection);
    }
}
