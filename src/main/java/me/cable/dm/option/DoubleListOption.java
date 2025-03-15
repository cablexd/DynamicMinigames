package me.cable.dm.option;

import me.cable.dm.option.abs.ListOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleListOption extends ListOption<Double> {

    @Override
    public @NotNull Object listSave(@NotNull Double val) {
        return val;
    }

    @Override
    public @Nullable Double listLoad(@NotNull Object object) {
        return (object instanceof Number v) ? v.doubleValue() : null;
    }
}
