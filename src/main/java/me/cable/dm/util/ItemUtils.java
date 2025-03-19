package me.cable.dm.util;

import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.bukkit.NamespacedKey;

import java.util.List;


public final class ItemUtils {

    public static @NotNull ItemStack item(@NotNull Material material, @Nullable String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (name != null) {
                meta.setItemName(Utils.formatColor(name));
            }
            if (lore != null) {
                List<String> formattedLore = lore.stream().map(Utils::formatColor).toList();
                meta.setLore(formattedLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static @NotNull ItemStack item(@NotNull ConfigurationSection cs) {
        Material material = Material.getMaterial(cs.getString("material", ""));
        return item(material == null ? Material.AIR : material, cs.getString("name"), cs.getStringList("lore"));
    }

    public static void setKey(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
    }

    public static boolean hasKey(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return (meta != null) && meta.getPersistentDataContainer().has(key);
    }
}
