package me.cable.dm.option;

import me.cable.dm.option.abs.Option;
import me.cable.dm.util.BlockReference;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    public @Nullable Object serialize() {
        BlockReference blockReference = _get();
        if (blockReference == null) return null;

        Map<String, Object> values = new HashMap<>();
        values.put("world", blockReference.worldName());
        values.put("x", blockReference.x());
        values.put("y", blockReference.y());
        values.put("z", blockReference.z());
        return values;
    }

    @Override
    public @Nullable BlockReference deserialize(@NotNull ConfigurationSection configurationSection) {
        String world = configurationSection.getString("world");

        return (world == null) ? null : new BlockReference(
                world,
                configurationSection.getInt("x"),
                configurationSection.getInt("y"),
                configurationSection.getInt("z")
        );
    }
}
