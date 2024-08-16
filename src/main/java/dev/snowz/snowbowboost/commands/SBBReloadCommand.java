package dev.snowz.snowbowboost.commands;

import dev.snowz.snowbowboost.SnowBowBoost;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SBBReloadCommand implements CommandExecutor {

    private static final SnowBowBoost plugin = SnowBowBoost.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        plugin.saveConfig();
        plugin.reloadConfig();
        sender.sendMessage("§aConfig reloaded!");
        return true;
    }
}
