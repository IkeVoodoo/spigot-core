package me.ikevoodoo.spigotcore.items.data;

import me.ikevoodoo.spigotcore.items.enchantment.ItemEnchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public record EnchantItemData(
        @NotNull Map<ItemEnchantment, Integer> enchantments
) {

    public static EnchantItemData empty() {
        return new EnchantItemData(Map.of());
    }

    public void apply(@NotNull ItemMeta meta) {
        for (var entry : this.enchantments.entrySet()) {
            meta.addEnchant(entry.getKey().toBukkit(), entry.getValue(), true);
        }
    }

    public static class Builder {
        private final Map<ItemEnchantment, Integer> enchantments = new EnumMap<>(ItemEnchantment.class);

        public Builder addEnchant(ItemEnchantment enchantment, int level) {
            this.enchantments.put(enchantment, level);
            return this;
        }

        public Builder removeEnchant(ItemEnchantment enchantment) {
            this.enchantments.remove(enchantment);
            return this;
        }

        public EnchantItemData build() {
            return new EnchantItemData(Collections.unmodifiableMap(new EnumMap<>(this.enchantments)));
        }
    }

}
