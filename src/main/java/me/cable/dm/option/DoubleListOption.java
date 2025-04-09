package me.cable.dm.option;

import me.cable.dm.option.abs.ListOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleListOption extends ListOption<Double> {

    @Override
    public @NotNull Object listSerialize(@NotNull Double v) {
        return v;
    }

    @Override
    public @Nullable Double listDeserialize(@NotNull Object object) {
        return (object instanceof Number v) ? v.doubleValue() : null;
    }
}
