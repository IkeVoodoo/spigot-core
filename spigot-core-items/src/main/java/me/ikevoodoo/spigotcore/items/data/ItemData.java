package me.ikevoodoo.spigotcore.items.data;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ItemData(
    @NotNull DestructionItemData destructionData,
    @NotNull DisplayItemData displayData,
    @NotNull EnchantItemData enchantData,
    @NotNull TagItemData tagData
) {

    public void apply(@NotNull ItemMeta meta) {
        this.displayData.apply(meta);
        this.enchantData.apply(meta);
        this.tagData.apply(meta);
    }

    public static class Builder {
        private DestructionItemData destructionData = DestructionItemData.neverDestroy();
        private DisplayItemData displayData = DisplayItemData.empty();
        private EnchantItemData enchantData = EnchantItemData.empty();
        private TagItemData tagData = TagItemData.empty();

        public Builder destructionData(DestructionItemData destructionData) {
            Objects.requireNonNull(destructionData);

            this.destructionData = destructionData;
            return this;
        }

        public Builder displayData(DisplayItemData displayData) {
            Objects.requireNonNull(displayData);

            this.displayData = displayData;
            return this;
        }

        public Builder enchantData(EnchantItemData enchantData) {
            Objects.requireNonNull(enchantData);

            this.enchantData = enchantData;
            return this;
        }

        public Builder tagData(TagItemData tagData) {
            Objects.requireNonNull(tagData);

            this.tagData = tagData;
            return this;
        }

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
