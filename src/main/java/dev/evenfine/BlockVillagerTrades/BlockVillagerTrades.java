package dev.evenfine.BlockVillagerTrades;

import dev.evenfine.BlockVillagerTrades.commands.ReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockVillagerTrades extends JavaPlugin implements Listener {
    private Set<BlockedItem> blockedTradesSet = new HashSet<>();
    private String mode;
    private Boolean blockWanderingTraderItems = false;

    private Set<Material> itemsWithEnchantments = new HashSet<>(Arrays.asList(
            Material.ENCHANTED_BOOK,
            Material.BOW,
            Material.CROSSBOW,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_AXE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_HOE,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.IRON_SWORD,
            Material.IRON_AXE,
            Material.IRON_PICKAXE,
            Material.IRON_SHOVEL,
            Material.IRON_HOE,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS));

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        getCommand("bvt").setExecutor(new ReloadCommand(this));
        loadConfiguration(null);
    }

    @EventHandler
    public void onTradeOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory))
            return;

        if (!blockWanderingTraderItems && merchantInventory.getMerchant() instanceof WanderingTrader) {
            return;
        }

        Merchant merchant = merchantInventory.getMerchant();
        List<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());

        if (mode.equals("block")) {
            recipes.forEach(recipe -> {
                if (isBlocked(recipe))
                    recipe.setMaxUses(0);
            });
        } else if (mode.equals("remove")) {
            recipes.removeIf(this::isBlocked);
            merchant.setRecipes(recipes);
        }
        // if every recipe is blocked, there is a unexpected behavior where the villager
        // is trading nothing and not tradable anymore
    }

    public void loadConfiguration(CommandSender sender) {
        try {
            reloadConfig();
            mode = "remove".equalsIgnoreCase(getConfig().getString("mode")) ? "remove" : "block";
            blockWanderingTraderItems = getConfig().getBoolean("block-wandering-trader-items", false);
            loadBlockedItems(sender);

            if (sender != null) {
                sender.sendMessage("§aConfiguration reloaded successfully.");
            }
            getLogger().info("Configuration reloaded successfully.");
        } catch (Exception e) {
            if (sender != null) {
                sender.sendMessage("§cError loading configuration. Check console for details.");
            }
            getLogger().warning("Error loading configuration. Check config.yml for errors.");
        }
    }

    private void loadBlockedItems(CommandSender sender) throws Exception {
        blockedTradesSet.clear();
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
                if (sender != null) {
                    sender.sendMessage(String.format("§cUnknown item/enchantment in config.yml: %s", obj));
                }

                getLogger().warning(String.format("Unknown item/enchantment in config.yml: %s", obj));
                throw new Exception();
            }
        }
    }

    private boolean isBlocked(@NotNull MerchantRecipe recipe) {
        Material resultType = recipe.getResult().getType();

        for (BlockedItem blockedItem : blockedTradesSet) {
            if (blockedItem.getMaterial() != resultType)
                continue;

            Set<PluginEnchantment> blockedEnchants = blockedItem.getEnchantments();
            if (blockedEnchants.isEmpty())
                return true;

            if (itemsWithEnchantments.contains(resultType)) {
                if (recipe.getResult().getItemMeta() == null)
                    return false;

                Map<Enchantment, Integer> itemEnchants = new HashMap<>(recipe.getResult().getItemMeta().getEnchants());

                if (recipe.getResult().getItemMeta() instanceof EnchantmentStorageMeta storageMeta) {
                    itemEnchants.putAll(storageMeta.getStoredEnchants());
                }

                for (PluginEnchantment blockedEnchant : blockedEnchants) {
                    if (itemEnchants.containsKey(blockedEnchant.getEnchantment())) {
                        Integer itemEnchantLevel = itemEnchants.get(blockedEnchant.getEnchantment());
                        Integer blockedEnchantLevel = blockedEnchant.getLevel();

                        if (blockedEnchantLevel == null || Objects.equals(itemEnchantLevel, blockedEnchantLevel)) {
                            return true;
                        }
                    }
                }
            } else {
                return true;
            }
        }

        return false;
    }
}