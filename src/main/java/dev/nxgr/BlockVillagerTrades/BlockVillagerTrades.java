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
    private Set<BlockedItem> blockedTradesSet = new HashSet<>();
    private Set<Enchantment> blockedTradesEnchantments;
    private String mode;

    private Set<Material> itemsWithEnchantments = new HashSet<>(Arrays.asList(
            Material.BOW,
            Material.CROSSBOW,
            Material.IRON_SWORD,
            Material.DIAMOND_SWORD,
            Material.IRON_AXE,
            Material.DIAMOND_AXE,
            Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.IRON_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.IRON_HOE,
            Material.DIAMOND_HOE,
            Material.IRON_HELMET,
            Material.DIAMOND_HELMET,
            Material.IRON_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.IRON_BOOTS,
            Material.DIAMOND_BOOTS));

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        loadBlockedItems();
        loadBlockedItems2();

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

    private void loadBlockedItems2() {
        List<?> blockedItemsRaw = getConfig().getList("blocked-items", Collections.emptyList());

        for (Object obj : blockedItemsRaw) {
            try {
                if (obj instanceof String materialName) {
                    blockedTradesSet.add(new BlockedItem(materialName));
                } else if (obj instanceof Map<?, ?> map) {

                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        String materialName = entry.getKey().toString();

                        BlockedItem blockedItem = new BlockedItem(materialName);

                        Object value = entry.getValue();
                        if (value instanceof Map<?, ?> nested) {
                            Object enchListObj = nested.get("enchantments");
                            if (enchListObj instanceof List<?> enchList) {
                                for (Object ench : enchList) {
                                    if (ench instanceof String enchName) {
                                        try {
                                            blockedItem.addEnchantment(enchName, null);
                                        } catch (Exception err) {
                                            getLogger().warning(String
                                                    .format("Unknown enchantment in config.yml: %s", enchName));
                                        }
                                    } else if (ench instanceof Map<?, ?> enchMap) {
                                        for (Map.Entry<?, ?> e : enchMap.entrySet()) {
                                            String enchName = e.getKey().toString();
                                            int level = Integer.parseInt(e.getValue().toString());
                                            try {
                                                blockedItem.addEnchantment(enchName, level);
                                            } catch (Exception err) {
                                                getLogger().warning(String
                                                        .format("Unknown enchantment in config.yml: %s", enchName));
                                            }
                                        }
                                    }
                                }
                                blockedTradesSet.add(blockedItem);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().warning(String.format("Unknown item/enchantment in config.yml: %s", obj));
            }
        }

        for (BlockedItem item : blockedTradesSet) {
            System.out.println(item.getMaterial());
            for (PluginEnchantment ench : item.getEnchantments()) {
                System.out.println(" - " + ench.getEnchantment() + ": " + ench.getLevel());
            }
            System.out.println("--------");
        }
    }

    private void loadBlockedItems() {
        List<String> itemNames = getConfig().getStringList("items");

        blockedTradesMaterials = new HashSet<>();
        blockedTradesEnchantments = new HashSet<>();

        Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess()
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