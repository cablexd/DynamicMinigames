package me.cable.dm.option.abs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ListOption<T> extends Option<List<T>> {

    @Override
    public @NotNull List<T> get() {
        if (_get() != null) {
            return List.copyOf(_get());
        }
        if (_getDefault() != null) {
            return List.copyOf(_getDefault());
        }

        throw new IllegalStateException("Option has no value");
    }

    public final @NotNull T get(int i) {
        return get().get(i);
    }

    public final void add(@NotNull T t) {
        if (_get() != null) {
            _get().add(t);
        }
    }

    public final int size() {
        return get().size();
    }

    public final void clear() {
        if (_get() != null) {
            _get().clear();
        }
        if (_getDefault() != null) {
            _getDefault().clear();
        }
    }

    @Override
    public final @Nullable Object serialize() {
        if (_get() == null) {
            // no value set: save default
            return null;
        }
        if (_get().isEmpty()) {
            return Collections.emptyList();
        }
        Object serializationExample = listSerialize(_get().getFirst());

        if (serializationExample instanceof ConfigurationSection || serializationExample instanceof Map<?, ?>) { // should be serialized as configuration sections
            // get configuration section from each element
            ConfigurationSection optionSection = new YamlConfiguration();
            int i = 0;

            for (T t : _get()) {
                Object o = listSerialize(t); // should be Map<String, Object>

                if (o instanceof ConfigurationSection elementCs) {
                    // set configuration section directly
                    optionSection.set(Integer.toString(i++), elementCs);
                } else if (o instanceof Map<?, ?> map) {
                    // set key/value pairs to newly created element section
                    ConfigurationSection elementCs = new YamlConfiguration();
                    map.forEach((k, v) -> elementCs.set(k.toString(), v));
                    optionSection.set(Integer.toString(i++), elementCs);
                } else {
                    throw new IllegalStateException("Invalid return: return must be a ConfigurationSection or a Map<String, Object>");
                }
            }

            return optionSection;
        }

        // serialize and add each element
        List<Object> l = new ArrayList<>();
        _get().forEach(v -> l.add(listSerialize(v)));
        return l;
    }

    @Override
    public final @Nullable List<T> deserialize(@NotNull Object object) {
        List<T> result = new ArrayList<>();

        if (object instanceof List<?> list) {
            for (Object o : list) {
                if (o == null) {
                    continue;
                }

                T t = listDeserialize(o);

                if (t != null) {
                    result.add(t);
                }
            }
        } else if (object instanceof ConfigurationSection configurationSection) {
            for (String key : configurationSection.getKeys(false)) {
                ConfigurationSection elementSection = configurationSection.getConfigurationSection(key);
                if (elementSection == null) continue;

                T t = listDeserialize((Object) elementSection);

                if (t != null) {
                    result.add(t);
                }
            }
        } else {
            return null;
        }

        return result;
    }

    @Override
    public final @Nullable List<T> deserialize(@NotNull ConfigurationSection configurationSection) {
        throw new UnsupportedOperationException("Should not be called");
    }

    /*
        Return Map<Object, String> to save as configuration section.
     */
    public @NotNull Object listSerialize(@NotNull T t) {
        return t;
    }

    /*
        Will provide configuration section if is a list of configuration section.
     */
    public @Nullable T listDeserialize(@NotNull Object object) {
        return (object instanceof ConfigurationSection configurationSection) ? listDeserialize(configurationSection) : null;
    }

    public @Nullable T listDeserialize(@NotNull ConfigurationSection configurationSection) {
        return null;
    }
}
