package me.cable.dm.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.cable.dm.util.Utils;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.World;

public class Leaderboard {

    private final String id;
    private final ConfigurationSection configurationSection;
    private final Supplier<List<Score>> scoreSupplier;

    private @Nullable Hologram hologram;

    public Leaderboard(@NotNull ConfigurationSection configurationSection, @NotNull Supplier<List<Score>> scoreSupplier) {
        id = UUID.randomUUID().toString();
        this.configurationSection = configurationSection;
        this.scoreSupplier = scoreSupplier;
    }

    private @Nullable Location getLocation() {
        String locString = configurationSection.getString("position");
        if (locString == null) return null;

        String[] parts = locString.split(",");
        World world = Bukkit.getWorld(parts[0]);

        return (world == null) ? null : new Location(
                world,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

    public final void update() {
        Location location = getLocation();

        if (!configurationSection.getBoolean("enabled") || location == null) {
            remove();
            return;
        }

        List<Score> scores = scoreSupplier.get();
        List<String> holoLines = new ArrayList<>();

        String title = configurationSection.getString("title");
        if (title != null) holoLines.add(title);

        String format = configurationSection.getString("format", "");
        int scorePerPage = configurationSection.getInt("entries", 10);
        List<String> excludedPlayers = configurationSection.getStringList("exclude");

        for (int i = 0; i < Math.min(scores.size(), scorePerPage); i++) {
            Score score = scores.get(i);
            UUID playerUuid = score.playerUuid();

            if (excludedPlayers.contains(playerUuid.toString())) {
                continue;
            }

            String playerName = Bukkit.getOfflinePlayer(playerUuid).getName();
            String line = format
                    .replace("{player}", playerName == null ? "N/A" : playerName)
                    .replace("{position}", Integer.toString(i + 1))
                    .replace("{score}", score.value());
            holoLines.add(Utils.formatColor(line));
        }

        if (hologram == null) {
            hologram = DHAPI.createHologram(id, location);
        } else if (!hologram.getLocation().equals(location)) {
            hologram.setLocation(location);
        }

        DHAPI.setHologramLines(hologram, holoLines);
    }

    public final void remove() {
        DHAPI.removeHologram(id);
    }

    public record Score(@NotNull UUID playerUuid, @NotNull String value) {

    }
}
