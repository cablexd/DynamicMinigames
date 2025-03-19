package me.cable.dm.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.World;

import org.bukkit.Bukkit;
import me.cable.dm.util.Utils;

public abstract class Leaderboard {

    private final String id;
    private final ConfigurationSection configurationSection;

    private @Nullable Hologram hologram;

    public Leaderboard(@NotNull ConfigurationSection configurationSection) {
        id = UUID.randomUUID().toString();
        this.configurationSection = configurationSection;
    }

    public abstract @NotNull List<Score> getScores();

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
        if (!configurationSection.getBoolean("enabled")) {
            return;
        }

        List<Score> scores = getScores();
        List<String> holoLines = new ArrayList<>();

        String title = configurationSection.getString("title");
        if (title != null) holoLines.add(title);

        String format = configurationSection.getString("format", "");
        int scorePerPage = configurationSection.getInt("entries", 10);
        List<String> excludedPlayers = configurationSection.getStringList("exclude");

        for (int i = 1; i <= Math.min(scores.size(), scorePerPage); i++) {
            Score score = scores.get(i);
            UUID playerUuid = score.playerUuid();

            if (excludedPlayers.contains(playerUuid.toString())) {
                continue;
            }

            String playerName = Bukkit.getOfflinePlayer(playerUuid).getName();
            String line = format
                    .replace("{player}", playerName == null ? "N/A" : playerName)
                    .replace("{position}", Integer.toString(i))
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

    public final void setLocation(@NotNull Location location) {
        this.location = location;
        update();
    }

    public record Score(@NotNull UUID playerUuid, @NotNull String value) {

    }
}
