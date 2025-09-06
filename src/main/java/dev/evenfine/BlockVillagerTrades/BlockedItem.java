package dev.evenfine.BlockVillagerTrades;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

public class BlockedItem {
    private Material material;
    private Set<PluginEnchantment> enchantments = new HashSet<PluginEnchantment>();
    private Registry<Enchantment> enchantmentRegistry;

    public BlockedItem(String materialName) {
        this.material = Material.valueOf(materialName.toUpperCase());

        this.enchantmentRegistry = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT);
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void addEnchantment(String enchantmentName, Integer level) {
        NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentName.toLowerCase());
        Enchantment ench = enchantmentRegistry.get(namespacedKey);

        if (ench == null) {
            throw new IllegalArgumentException();
        }

        this.enchantments.add(new PluginEnchantment(ench, level));
    }

    public Set<PluginEnchantment> getEnchantments() {
        return enchantments;
    }
}
