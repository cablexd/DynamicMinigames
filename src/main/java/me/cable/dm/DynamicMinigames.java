package me.cable.dm;

import me.cable.dm.commands.MainCommand;
import me.cable.dm.commands.MinigameCommand;
import me.cable.dm.minigame.Minigame;
import me.cable.dm.minigame.provided.FourCornersMinigame;
import me.cable.dm.minigame.IntermissionMinigame;
import me.cable.dm.minigame.provided.TrampolineMinigame;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DynamicMinigames extends JavaPlugin {

    private MinigameManager minigameManager;
    private MinigameSerializer minigameSerializer;

    /*
        TODO:
        default values
     */

    @Override
    public void onEnable() {
        initializeHandlers();
        registerCommands();

        Minigame.initializeTimer(this);
        IntermissionMinigame.initialize(this);
        registerMinigames();
        minigameSerializer.loadMinigames();
    }

    @Override
    public void onDisable() {
        minigameSerializer.saveMinigames();
    }

    private void initializeHandlers() {
        minigameManager = new MinigameManager();
        minigameSerializer = new MinigameSerializer(this);
    }

    private void registerCommands() {
        new MainCommand().register("dynamicminigames");
        new MinigameCommand().register("minigame");
    }

    private void registerMinigames() {
        MinigameManager.registerMinigame("four_corners", FourCornersMinigame::new);
        MinigameManager.registerMinigame("trampoline", TrampolineMinigame::new);
    }

    public @NotNull MinigameManager getMinigameManager() {
        return minigameManager;
    }

    public @NotNull MinigameSerializer getMinigameSerializer() {
        return minigameSerializer;
    }
}
