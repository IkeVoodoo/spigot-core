package me.ikevoodoo.spigotcore.items.data;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public record TagItemData(
    @NotNull Set<ItemFlag> flags
) {

    /**
     * Returns completely empty tag data.
     *
     * @return New tag item data with an immutable empty flag set.
     * @since 1.0.0
     * */
    @NotNull
    public static TagItemData empty() {
        return new TagItemData(Set.of());
    }

    /**
     * Sets the provided item flags on the item meta.
     *
     * @param itemMeta The target item meta, must not be null.
     * @since 1.0.0
     * */
    public void apply(@NotNull ItemMeta itemMeta) {
        Objects.requireNonNull(itemMeta, "Cannot set item flags on a null item meta!");

        for (var flag : this.flags) {
            itemMeta.addItemFlags(flag);
        }
    }

    /**
     * Builder for {@link TagItemData}
     *
     * @author IkeVoodoo
     * @since 1.0.0
     * */
    public static class Builder {

        private final Set<ItemFlag> flags = EnumSet.noneOf(ItemFlag.class);

        /**
         * Adds the provided flags to the set.
         *
         * @param flags An array of item flags must not be null.
         * @since 1.0.0
         *
         * @see ItemFlag
         *
         * @return this builder instance.
         * */
        public Builder addFlags(@NotNull ItemFlag... flags) {
            Objects.requireNonNull(flags, "Item flags must not be null!");

            Collections.addAll(this.flags, flags);
            return this;
        }

        /**
         * Removes the provided flags from the set.
         *
         * @param flags An array of item flags must not be null.
         * @since 1.0.0
         *
         * @see ItemFlag
         *
         * @return this builder instance.
         * */
        public Builder removeFlags(@NotNull ItemFlag... flags) {
            Objects.requireNonNull(flags, "Item flags must not be null!");

            for (var flag : flags) {
                this.flags.remove(flag);
            }

            return this;
        }

        /**
         * Builds a new tag item data with an unmodifiable set.
         *
         * @since 1.0.0
         *
         * @return A new tag item data.
         * */
        @NotNull
        public TagItemData build() {
            return new TagItemData(Collections.unmodifiableSet(EnumSet.copyOf(this.flags)));
        }

    }

}
