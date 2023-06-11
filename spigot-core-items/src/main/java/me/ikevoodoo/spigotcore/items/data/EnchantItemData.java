package me.ikevoodoo.spigotcore.items.data;

import me.ikevoodoo.spigotcore.items.enchantment.ItemEnchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public record EnchantItemData(
        @NotNull Map<ItemEnchantment, Integer> enchantments
) {

    /**
     * Returns completely enchant item data.
     *
     * @return New enchant item data with an immutable empty enchantment map.
     * @since 1.0.0
     * */
    @NotNull
    public static EnchantItemData empty() {
        return new EnchantItemData(Map.of());
    }

    /**
     * Applies the provided enchantments on the item meta, ignoring level restriction.
     *
     * @param meta The target item meta, must not be null.
     * @since 1.0.0
     * */
    public void apply(@NotNull ItemMeta meta) {
        Objects.requireNonNull(meta, "Cannot apply enchant item data to a null item meta!");

        for (var entry : this.enchantments.entrySet()) {
            meta.addEnchant(entry.getKey().toBukkit(), entry.getValue(), true);
        }
    }

    /**
     * Builder for {@link EnchantItemData}
     *
     * @author IkeVoodoo
     * @since 1.0.0
     * */
    public static class Builder {
        private final Map<ItemEnchantment, Integer> enchantments = new EnumMap<>(ItemEnchantment.class);

        /**
         * Add an enchantment with a level.
         *
         * @param enchantment The enchantment to add, must not be null.
         * @param level The level of the enchantment, range 0-255
         * @since 1.0.0
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder addEnchant(@NotNull ItemEnchantment enchantment, int level) {
            Objects.requireNonNull(enchantment, "Cannot add a null enchantment!");
            if (level < 0 || level > 255) {
                throw new IllegalArgumentException("Cannot add enchant with a level of %s, it must be in ranges 0-255".formatted(level));
            }

            this.enchantments.put(enchantment, level);
            return this;
        }

        /**
         * Removes an enchantment.
         *
         * @param enchantment The enchantment to remove, must not be null.
         * @since 1.0.0
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder removeEnchant(@NotNull ItemEnchantment enchantment) {
            Objects.requireNonNull(enchantment, "Cannot remove a null enchantment!");

            this.enchantments.remove(enchantment);
            return this;
        }

        /**
         * Builds new enchant item data with an unmodifiable map.
         *
         * @since 1.0.0
         *
         * @return A new enchant item data.
         * */
        @NotNull
        public EnchantItemData build() {
            return new EnchantItemData(Collections.unmodifiableMap(new EnumMap<>(this.enchantments)));
        }
    }

}
