package dev.nxgr.VillagerDontSellBooks;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.util.List;


public class VillagerDontSellBooks extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onTradeOpen(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        List<MerchantRecipe> recipes = villager.getRecipes();
        for (MerchantRecipe recipe : recipes) {
            Material resultType = recipe.getResult().getType();

            if (resultType == Material.BOOK || resultType == Material.ENCHANTED_BOOK) {
                recipe.setMaxUses(0);
            }
        }
    }
}