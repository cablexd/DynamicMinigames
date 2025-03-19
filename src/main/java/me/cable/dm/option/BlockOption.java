package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockReference;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockOption extends Option<BlockReference> {

    @Override
    public boolean canSetInGame() {
        return true;
    }

    @Override
    public @Nullable String setInGame(@NotNull Player player, @NotNull String[] args) {
        Block block = player.getTargetBlock(null, 20);
        set(new BlockReference(block));
        player.sendMessage(ChatColor.GREEN + "Set value to the block you are currently looking at: "
                + ChatColor.GOLD + block.getX() + ", " + block.getY() + ", " + block.getZ());
        return null;
    }

    @Override
    public boolean useConfigurationSection() {
        return true;
    }

    @Override
    public boolean save(@NotNull ConfigurationSection configurationSection) {
        BlockReference blockReference = getRaw();
        if (blockReference == null) return false;

        configurationSection.set("world", blockReference.worldName());
        configurationSection.set("x", blockReference.x());
        configurationSection.set("y", blockReference.y());
        configurationSection.set("z", blockReference.z());
        return true;
    }

    @Override
    public void load(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");
        if (world == null) throw new IllegalStateException("Missing world name");

        setRaw(new BlockReference(
                world,
                configurationSection.getInt("x"),
                configurationSection.getInt("y"),
                configurationSection.getInt("z")
        ));
    }
}
