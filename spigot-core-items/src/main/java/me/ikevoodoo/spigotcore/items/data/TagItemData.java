package me.ikevoodoo.spigotcore.items.data;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record TagItemData(
    @NotNull Set<ItemFlag> flags
) {

    public static TagItemData empty() {
        return new TagItemData(Set.of());
    }

    public void apply(@NotNull ItemMeta itemMeta) {
        for (var flag : this.flags) {
            itemMeta.addItemFlags(flag);
        }
    }

    public static class Builder {

        private final Set<ItemFlag> flags = EnumSet.noneOf(ItemFlag.class);

        public Builder addFlags(ItemFlag... flags) {
            Collections.addAll(this.flags, flags);
            return this;
        }

        public Builder removeFlags(ItemFlag... flags) {
            for (var flag : flags) {
                this.flags.remove(flag);
            }

            return this;
        }

        public TagItemData build() {
            return new TagItemData(Collections.unmodifiableSet(EnumSet.copyOf(this.flags)));
        }

    }

}
