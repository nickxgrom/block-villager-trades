package dev.nxgr.BlockVillagerTrades;

import org.bukkit.enchantments.Enchantment;

public class PluginEnchantment {
    private Enchantment enchantment;
    private Integer level;

    public PluginEnchantment(Enchantment enchantment, Integer level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public Integer getLevel() {
        return level;
    }
}
