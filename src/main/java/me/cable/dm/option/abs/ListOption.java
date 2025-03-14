package me.cable.dm.option.abs;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public non-sealed abstract class ListOption<T> extends AbstractOption {

    private final List<T> list = new ArrayList<>();

    public final @NotNull List<T> get() {
        return List.copyOf(list);
    }

    public final @NotNull T get(int i) {
        return list.get(i);
    }

    public final void add(@NotNull T value) {
        list.add(value);
    }

    public final @NotNull T remove(int i) {
        return list.remove(i);
    }

    public final int size() {
        return list.size();
    }

    public final void clear() {
        list.clear();
    }

    @Override
    public final @NotNull Object save() {
        // not using configuration section: save to single list
        List<Object> l = new ArrayList<>();

        for (T t : list) {
            l.add(listSave(t));
        }

        return l;
    }

    @Override
    public final boolean save(@NotNull ConfigurationSection configurationSection) {
        // using configuration section: save each item to own configuration section
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            ConfigurationSection csItem = configurationSection.createSection(Integer.toString(i));
            listSave(t, csItem);
        }

        return true;
    }

    @Override
    public final void load(@Nullable Object object) {
        // not using configuration section: load items from single list
        if (object instanceof List<?> l) {
            for (Object o : l) {
                T t = listLoad(o);

                if (t != null) {
                    add(t);
                }
            }
        }
    }

    @Override
    public final void load(@NotNull ConfigurationSection configurationSection) {
        // using configuration section: load each item from own configuration section
        for (String key : configurationSection.getKeys(false)) {
            ConfigurationSection cs = configurationSection.getConfigurationSection(key);

            if (cs != null) {
                T t = listLoad(cs);

                if (t != null) {
                    add(t);
                }
            }
        }
    }

    public @NotNull Object listSave(@NotNull T t) {
        return t;
    }

    public void listSave(@NotNull T t, @NotNull ConfigurationSection configurationSection) {
        // empty
    }

    public @Nullable T listLoad(@NotNull Object object) {
        return null;
    }

    public @Nullable T listLoad(@NotNull ConfigurationSection configurationSection) {
        return null;
    }
}
