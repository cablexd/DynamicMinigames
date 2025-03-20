package me.cable.dm.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.cable.dm.option.abs.Option;
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

    private final String holoId;
    private final Supplier<List<Score>> scoreSupplier;

    private boolean enabled = false;
    private @NotNull String position = "world,0.0,0.0,0.0";
    private @Nullable String title;
    private @Nullable String format;
    private @Nullable Integer entries;
    private @NotNull List<String> exclude = new ArrayList<>();

    private @Nullable Hologram hologram;

    public Leaderboard(@NotNull Supplier<List<Score>> scoreSupplier) {
        holoId = UUID.randomUUID().toString();
        this.scoreSupplier = scoreSupplier;
    }

    private @Nullable Location getLocation() {
        try {
            String[] parts = position.split(",");
            World world = Bukkit.getWorld(parts[0]);

            return (world == null) ? null : new Location(
                    world,
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
            );
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private @NotNull String getFormat() {
        return format == null ? "" : format; // TODO: get default
    }

    private int getEntries() {
        return entries == null ? 10 : entries; // TODO: get default
    }

    public void update() {
        Location location = getLocation();

        if (!enabled || location == null) {
            remove();
            return;
        }

        List<Score> scores = scoreSupplier.get();
        List<String> holoLines = new ArrayList<>();

        if (title != null) holoLines.add(title);

        for (int i = 0; i < Math.min(scores.size(), getEntries()); i++) {
            Score score = scores.get(i);
            UUID playerUuid = score.playerUuid();

            if (exclude.contains(playerUuid.toString())) {
                continue;
            }

            String playerName = Bukkit.getOfflinePlayer(playerUuid).getName();
            String line = getFormat()
                    .replace("{player}", playerName == null ? "N/A" : playerName)
                    .replace("{position}", Integer.toString(i + 1))
                    .replace("{score}", score.value());
            holoLines.add(Utils.formatColor(line));
        }

        if (hologram == null) {
            hologram = DHAPI.createHologram(holoId, location);
        } else if (!hologram.getLocation().equals(location)) {
            hologram.setLocation(location);
        }

        DHAPI.setHologramLines(hologram, holoLines);
    }

    private @NotNull Object getValue(@Nullable Object v) {
        return (v == null) ? Option.USE_DEFAULT : v;
    }

    private <T> @Nullable T checkValue(@Nullable T t) {
        return (t instanceof String s && s.equals(Option.USE_DEFAULT)) ? null : t;
    }

    public void saveSettings(@NotNull ConfigurationSection cs) {
        cs.set("enabled", enabled);
        cs.set("position", position);
        cs.set("title", getValue(title));
        cs.set("format", getValue(format));
        cs.set("entries", getValue(entries));

        List<String> excludeSave = new ArrayList<>(exclude);

        if (exclude.isEmpty()) {
            excludeSave.add("player_uuid");
        }

        cs.set("exclude", excludeSave);
    }

    public void loadSettings(@NotNull ConfigurationSection cs) {
        setEnabled(cs.getBoolean("enabled"));

        String position = cs.getString("position");
        if (position != null) setPosition(position);

        setTitle(checkValue(cs.getString("title")));
        setFormat(checkValue(cs.getString("format")));
        setEntries(cs.isInt("entries") ? cs.getInt("entries") : null);
        setExclude(cs.getStringList("exclude"));

        update();
    }

    public void remove() {
        DHAPI.removeHologram(holoId);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        update();
    }

    public void setPosition(@NotNull String position) {
        this.position = position;
        update();
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
        update();
    }

    public void setFormat(@Nullable String format) {
        this.format = format;
        update();
    }

    public void setEntries(@Nullable Integer entries) {
        this.entries = entries;
        update();
    }

    public void setExclude(@NotNull List<String> exclude) {
        this.exclude = exclude;
        update();
    }

    public record Score(@NotNull UUID playerUuid, @NotNull String value) {

    }
}
