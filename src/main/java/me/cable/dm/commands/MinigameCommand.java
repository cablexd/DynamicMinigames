package me.cable.dm.commands;

import me.cable.dm.MinigameManager;
import me.cable.dm.minigame.Minigame;
import me.cable.dm.option.IntegerOption;
import me.cable.dm.option.StringOption;
import me.cable.dm.option.abs.AbstractOption;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MinigameCommand extends AbstractCommand {

    private final MinigameManager minigameManager;

    public MinigameCommand() {
        minigameManager = dynamicMinigames.getMinigameManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String usage = ChatColor.RED + "Usage: /" + label + " <create|list|option|remove>";

        if (args.length < 1) {
            commandSender.sendMessage(usage);
            return true;
        }

        switch (args[0]) {
            case "create" -> {
                if (args.length < 3) {
                    commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " create <minigame_type> <minigame_id>");
                    return true;
                }

                String minigameType = args[1];
                String minigameId = args[2];

                if (!minigameManager.isValidMinigameType(minigameType)) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid minigame type " + ChatColor.GOLD + minigameType + ChatColor.RED + ".");
                    return true;
                }
                if (!minigameManager.isMinigameIdAvailable(minigameType, minigameId)) {
                    commandSender.sendMessage(ChatColor.RED + "The minigame ID " + ChatColor.GOLD + minigameId + ChatColor.RED + " of type "
                            + ChatColor.GOLD + minigameType + ChatColor.RED + " already exists!");
                    return true;
                }

                Minigame minigame = minigameManager.createMinigame(minigameType, minigameId);
                minigame.initializeLeaderboards(new YamlConfiguration());

                commandSender.sendMessage(ChatColor.GREEN + "Successfully created minigame " + ChatColor.GOLD + minigameId + ChatColor.GREEN + " of type "
                        + ChatColor.GOLD + minigameType + ChatColor.GREEN + ".");
            }
            case "list" -> {
                Map<String, List<String>> minigames = minigameManager.getCreatedMinigames();

                if (minigames.isEmpty()) {
                    commandSender.sendMessage(ChatColor.GREEN + "No minigames have been created.");
                } else {
                    StringBuilder sb = new StringBuilder();

                    for (Map.Entry<String, List<String>> entry : minigames.entrySet()) {
                        if (!sb.isEmpty()) {
                            sb.append('\n');
                        }

                        sb.append(entry.getKey());

                        for (String s : entry.getValue()) {
                            sb.append('\n').append("  > ").append(s);
                        }
                    }

                    commandSender.sendMessage(sb.toString());
                }
            }
            case "option" -> {
                usage = ChatColor.RED + "Usage: /" + label + " option <minigame_type> <minigame_id> <option>";

                if (args.length < 4) {
                    commandSender.sendMessage(usage);
                    return true;
                }

                String minigameType = args[1];
                String minigameId = args[2];

                Minigame minigame = minigameManager.getMinigame(minigameType, minigameId);

                if (minigame == null) {
                    commandSender.sendMessage(ChatColor.RED + "The minigame " + ChatColor.GOLD + minigameId + ChatColor.RED + " of type "
                            + ChatColor.GOLD + minigameType + ChatColor.RED + " does not exist!");
                    return true;
                }

                String optionId = args[3];
                AbstractOption abstractOption = minigame.getOptions().get(optionId);

                if (abstractOption == null) {
                    commandSender.sendMessage(ChatColor.RED + "The option " + ChatColor.GOLD + optionId + ChatColor.RED + " does not exist!");
                    return true;
                }

                if (!abstractOption.canSetInGame()) {
                    commandSender.sendMessage(ChatColor.RED + "This option can only be modified in the config file!");
                    return true;
                }

                String[] extraArgs;

                if (args.length == 4) {
                    extraArgs = new String[0];
                } else {
                    extraArgs = Arrays.copyOfRange(args, 4, args.length);
                }

                String appendToUsage = abstractOption.setInGame(commandSender, extraArgs);

                if (appendToUsage != null) {
                    commandSender.sendMessage(usage + appendToUsage);
                }

                if (false) switch (abstractOption) {
                    case null ->
                            commandSender.sendMessage(ChatColor.RED + "The option " + ChatColor.GOLD + optionId + ChatColor.RED + " does not exist!");
                    case StringOption stringOption -> {
                        if (args.length < 5) {
                            commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " option <minigame type> <minigame id> <option> <value>");
                            return true;
                        }

                        String value = args[4];
                        stringOption.set(value);
                        commandSender.sendMessage(ChatColor.GREEN + "The option " + ChatColor.GOLD + optionId + ChatColor.GREEN + " has been set to "
                                + ChatColor.GOLD + value + ChatColor.GREEN + ".");
                    }
                    case IntegerOption integerOption -> {
                        if (args.length < 5) {
                            commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " option <minigame type> <minigame id> <option> <integer value>");
                            return true;
                        }

                        int value;

                        try {
                            value = Integer.parseInt(args[4]);
                        } catch (NumberFormatException ex) {
                            commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " option <minigame type> <minigame id> <option> <integer value>");
                            return true;
                        }

                        integerOption.set(value);
                        commandSender.sendMessage(ChatColor.GREEN + "The option " + ChatColor.GOLD + optionId + ChatColor.GREEN + " has been set to "
                                + ChatColor.GOLD + value + ChatColor.GREEN + ".");
                    }
                    default ->
                            commandSender.sendMessage(ChatColor.RED + "That option's value cannot be set through this command!");
                }
            }
            case "remove" -> {
                if (args.length < 3) {
                    commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " remove <minigame_type> <minigame_id>");
                    return true;
                }

                String minigameType = args[1];
                String minigameId = args[2];

                if (!minigameManager.isValidMinigameType(minigameType)) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid minigame type " + ChatColor.GOLD + minigameType + ChatColor.RED + ".");
                    return true;
                }
                if (minigameManager.isMinigameIdAvailable(minigameType, minigameId)) {
                    commandSender.sendMessage(ChatColor.RED + "The minigame ID " + ChatColor.GOLD + minigameId + ChatColor.RED + " of type "
                            + ChatColor.GOLD + minigameType + ChatColor.RED + " does not exist!");
                    return true;
                }

                minigameManager.removeMinigame(minigameType, minigameId);
                commandSender.sendMessage(ChatColor.GREEN + "Successfully removed minigame " + ChatColor.GOLD + minigameId + ChatColor.GREEN + " of type "
                        + ChatColor.GOLD + minigameType + ChatColor.GREEN + ".");
            }
            default -> commandSender.sendMessage(usage);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> result = new ArrayList<>();

        if (strings.length == 1) {
            for (String string : List.of("create", "list", "option", "remove")) {
                if (string.startsWith(strings[0])) {
                    result.add(string);
                }
            }
        }

        return result;
    }
}
