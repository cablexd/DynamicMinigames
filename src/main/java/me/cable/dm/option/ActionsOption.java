package me.cable.dm.option;

import me.cable.actionsapi.Actions;
import org.jetbrains.annotations.NotNull;

public class ActionsOption extends StringListOption {

    public @NotNull Actions actions() {
        return new Actions(get());
    }
}
