package me.cable.dm.option;

import me.cable.dm.option.abs.ListOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerListOption extends ListOption<Integer> {

    @Override
    public @NotNull Object listSerialize(@NotNull Integer val) {
        return val;
    }

    @Override
    public @Nullable Integer listDeserialize(@NotNull Object object) {
        return (object instanceof Integer val) ? val : null;
    }
}
