package me.ikevoodoo.spigotcore.items.data;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public record ItemData(
    @NotNull DestructionItemData destructionData,
    @NotNull DisplayItemData displayData,
    @NotNull EnchantItemData enchantData,
    @NotNull TagItemData tagData
) {

    /**
     * Applies every item data to an item meta.
     *
     * @param meta The target item meta, must not be null.
     * @since 1.0.0
     *
     * @see DisplayItemData
     * @see EnchantItemData
     * @see TagItemData
     * */
    public void apply(@NotNull ItemMeta meta) {
        Objects.requireNonNull(meta, "Cannot apply item data to a null item meta!");

        this.displayData.apply(meta);
        this.enchantData.apply(meta);
        this.tagData.apply(meta);
    }

    /**
     * Builder for {@link ItemData}
     *
     * @author IkeVoodoo
     * @since 1.0.0
     * */
    public static class Builder {
        private DestructionItemData destructionData = DestructionItemData.neverDestroy();
        private DisplayItemData displayData = DisplayItemData.empty();
        private EnchantItemData enchantData = EnchantItemData.empty();
        private TagItemData tagData = TagItemData.empty();

        /**
         * Sets the destruction data.
         *
         * @param destructionData The item destruction data, must not be null.
         * @since 1.0.0
         *
         * @see DestructionItemData
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder destructionData(@NotNull DestructionItemData destructionData) {
            Objects.requireNonNull(destructionData, "Cannot set null destruction data!");

            this.destructionData = destructionData;
            return this;
        }

        /**
         * Sets the display data.
         *
         * @param displayData The item display data, must not be null.
         * @since 1.0.0
         *
         * @see DisplayItemData
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder displayData(@NotNull DisplayItemData displayData) {
            Objects.requireNonNull(displayData, "Cannot set null display data!");

            this.displayData = displayData;
            return this;
        }

        /**
         * Sets the enchant data.
         *
         * @param enchantData The item enchant data, must not be null.
         * @since 1.0.0
         *
         * @see EnchantItemData
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder enchantData(@NotNull EnchantItemData enchantData) {
            Objects.requireNonNull(enchantData, "Cannot set null enchant data!");

            this.enchantData = enchantData;
            return this;
        }

        /**
         * Sets the tag data.
         *
         * @param tagData The item tag data must not be null.
         * @since 1.0.0
         *
         * @see TagItemData
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder tagData(@NotNull TagItemData tagData) {
            Objects.requireNonNull(tagData, "Cannot set null tag data!");

            this.tagData = tagData;
            return this;
        }

        /**
         * Builds new item data.
         *
         * @since 1.0.0
         *
         * @see DestructionItemData
         * @see DisplayItemData,
         * @see EnchantItemData
         * @see TagItemData
         *
         * @return A new item data.
         * */
        @NotNull
        public ItemData build() {
            return new ItemData(
                    this.destructionData,
                    this.displayData,
                    this.enchantData,
                    this.tagData
            );
        }
    }

}
