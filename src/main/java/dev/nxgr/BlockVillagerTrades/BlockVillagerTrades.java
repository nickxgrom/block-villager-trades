package dev.nxgr.BlockVillagerTrades;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull; 

import java.util.*;

public class BlockVillagerTrades extends JavaPlugin implements Listener {
    private Set<Material> blockedTradesMaterials;
    private Set<Enchantment> blockedTradesEnchantments;
    private String mode;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        loadBlockedItems();

        mode = "delete".equalsIgnoreCase(getConfig().getString("mode")) ? "delete" : "block";
    }

    @EventHandler
    public void onTradeOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory))
            return;

        Merchant merchant = merchantInventory.getMerchant();
        List<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());

        if (mode.equals("block")) {
            recipes.forEach(recipe -> {
                if (isBlocked(recipe))
                    recipe.setMaxUses(0);
            });
        } else {
            recipes.removeIf(this::isBlocked);
            merchant.setRecipes(recipes);
        }
    }

    private void loadBlockedItems() {
        List<String> itemNames = getConfig().getStringList("items");

        blockedTradesMaterials = new HashSet<>();
        blockedTradesEnchantments = new HashSet<>();

        Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT);

        for (String name : itemNames) {
            try {
                String[] materialParts = name.split(":");
                Material material = Material.valueOf(materialParts[0].toUpperCase());

                if (material == Material.ENCHANTED_BOOK && materialParts.length > 1) {
                    NamespacedKey namespacedKey = NamespacedKey.minecraft(materialParts[1].toLowerCase());

                    Enchantment ench = enchantmentRegistry.get(namespacedKey);

                    if (ench == null) {
                        throw new IllegalArgumentException("Unknown enchantment");
                    } else {
                        blockedTradesEnchantments.add(ench);
                    }
                } else {
                    blockedTradesMaterials.add(material);
                }
            } catch (IllegalArgumentException e) {
                getLogger().warning(String.format("Unknown item/enchantment in config.yml: %s", name));
            }
        }
    }

    private boolean isBlocked(@NotNull MerchantRecipe recipe) {
        Material resultType = recipe.getResult().getType();

        if (blockedTradesMaterials.contains(resultType))
            return true;

        if (resultType == Material.ENCHANTED_BOOK &&
                recipe.getResult().getItemMeta() instanceof EnchantmentStorageMeta meta) {
            return blockedTradesEnchantments.stream().anyMatch(meta::hasStoredEnchant);
        }

        return false;
    }
}