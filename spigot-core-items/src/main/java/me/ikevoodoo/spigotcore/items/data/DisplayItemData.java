package me.ikevoodoo.spigotcore.items.data;

import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public record DisplayItemData(
        @Nullable String displayName,
        @NotNull List<String> lore,

        int customModelData
) {

    /**
     * Returns completely display item data.
     *
     * @return New display item data, with a null display name, immutable empty lore and custom model data as 0
     * @since 1.0.0
     * */
    public static DisplayItemData empty() {
        return new DisplayItemData(null, List.of(), 0);
    }

    /**
     * Sets the item meta's display name, lore and custom model data.
     *
     * @param meta The target item meta, must not be null.
     * @since 1.0.0
     * */
    public void apply(@NotNull ItemMeta meta) {
        Objects.requireNonNull(meta, "Cannot apply display item data to a null item meta!");

        meta.setDisplayName(this.displayName);
        meta.setLore(this.lore);
        meta.setCustomModelData(this.customModelData);
    }

    /**
     * Builder for {@link DisplayItemData}
     *
     * @author IkeVoodoo
     * @since 1.0.0
     * */
    public static class Builder {

        private String displayName;
        private List<String> lore = List.of();
        private int customModelData;

        /**
         * Sets the display name.
         *
         * @param displayName The item display name.
         * @since 1.0.0
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder displayName(@Nullable String displayName) {
            this.displayName = displayName == null ? null : ChatColor.RESET + displayName;
            return this;
        }

        /**
         * Sets the lore, each line is forced to start with {@link ChatColor#RESET}
         *
         * @param lore The item lore must not be null.
         * @since 1.0.0
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder lore(@NotNull List<String> lore) {
            Objects.requireNonNull(lore, "Lore list must not be null!");

            this.lore = lore.stream().map(s -> ChatColor.RESET + s).toList();
            return this;
        }

        /**
         * Sets the custom model data.
         *
         * @param customModelData The item custom model data, must be >= 0.
         * @since 1.0.0
         *
         * @return this builder instance.
         * */
        @NotNull
        public Builder customModelData(int customModelData) {
            if (customModelData < 0) {
                throw new IllegalArgumentException("Custom model data must be >= 0");
            }

            this.customModelData = customModelData;
            return this;
        }

        /**
         * Builds new display item data.
         *
         * @since 1.0.0
         *
         * @return A new display item data.
         * */
        @NotNull
        public DisplayItemData build() {
            return new DisplayItemData(
                    this.displayName,
                    Collections.unmodifiableList(this.lore),
                    this.customModelData
            );
        }
    }

}
