package me.cable.dm.option.abs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public non-sealed abstract class Option<T> extends AbstractOption {

    private @Nullable T t;
    private @Nullable T volatileValue;

    private void onValueChange() {
        // TODO: alert minigame
    }

    public final @NotNull T get() {
        if (volatileValue != null) {
            return volatileValue;
        }
        if (t == null) {
            // TODO: get default value
            return null;
        }

        return t;
    }

    public final void set(@Nullable T value) {
        t = value;
        onValueChange();
    }

    protected final @Nullable T getRaw() {
        return t;
    }

    protected final void setRaw(@Nullable T t) {
        this.t = t;
    }

    public boolean supportsVolatile() {
        return false;
    }

    public final void setVolatileValue(@Nullable T t) {
        if (!supportsVolatile()) {
            throw new IllegalStateException("Option does not support volatile values");
        }

        volatileValue = t;
        onValueChange();
    }
}
