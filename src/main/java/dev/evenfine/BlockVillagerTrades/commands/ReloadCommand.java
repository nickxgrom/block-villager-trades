package dev.evenfine.BlockVillagerTrades.commands;

import dev.evenfine.BlockVillagerTrades.BlockVillagerTrades;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final BlockVillagerTrades plugin;

    public ReloadCommand(BlockVillagerTrades plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("blockvillagertrades.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§cUsage: /bvt reload");
            return true;
        }

        plugin.loadConfiguration(sender);

        return true;
    }
}
