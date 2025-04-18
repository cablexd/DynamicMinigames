package me.cable.dm;

import me.cable.dm.commands.MainCommand;
import me.cable.dm.commands.MinigameCommand;
import me.cable.dm.leaderboard.LeaderboardData;
import me.cable.dm.minigame.Minigame;
import me.cable.dm.minigame.provided.*;
import me.cable.dm.minigame.IntermissionMinigame;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DynamicMinigames extends JavaPlugin {

    private MinigameManager minigameManager;
    private MinigameSerializer minigameSerializer;
    private LeaderboardData leaderboardData;

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
        leaderboardData = new LeaderboardData(this);
    }

    private void registerCommands() {
        new MainCommand().register("dynamicminigames");
        new MinigameCommand().register("minigame");
    }

    private void registerMinigames() {
        MinigameManager.registerMinigame("four_corners", FourCornersMinigame::new);
        MinigameManager.registerMinigame("highblock", HighblockMinigame::new);
        MinigameManager.registerMinigame("race", RaceMinigame::new);
        MinigameManager.registerMinigame("spleef", SpleefMinigame::new);
        MinigameManager.registerMinigame("trampoline", TrampolineMinigame::new);
    }

    public @NotNull MinigameManager getMinigameManager() {
        return minigameManager;
    }

    public @NotNull MinigameSerializer getMinigameSerializer() {
        return minigameSerializer;
    }

    public @NotNull LeaderboardData getLeaderboardData() {
        return leaderboardData;
    }
}
