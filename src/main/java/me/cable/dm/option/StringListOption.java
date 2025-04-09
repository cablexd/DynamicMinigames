package me.cable.dm.option;

import me.cable.dm.option.abs.ListOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringListOption extends ListOption<String> {

    @Override
    public @NotNull Object listSerialize(@NotNull String string) {
        return string;
    }

    @Override
    public @Nullable String listDeserialize(@NotNull Object object) {
        return (object instanceof String string) ? string : null;
    }
}
