package me.ikevoodoo.spigotcore.items.data;

import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record DisplayItemData(
        @Nullable String displayName,
        @NotNull List<String> lore,

        int customModelData
) {

    public static DisplayItemData empty() {
        return new DisplayItemData(null, List.of(), 0);
    }

    public void apply(@NotNull ItemMeta meta) {
        meta.setDisplayName(this.displayName);
        meta.setLore(this.lore);
        meta.setCustomModelData(this.customModelData);
    }

    public static class Builder {

        private String displayName;
        private List<String> lore = List.of();
        private int customModelData;

        public Builder displayName(String displayName) {
            this.displayName = displayName == null ? null : ChatColor.RESET + displayName;
            return this;
        }

        public Builder lore(List<String> lore) {
            if (lore == null) return this;

            this.lore = lore.stream().map(s -> ChatColor.RESET + s).toList();
            return this;
        }

        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public DisplayItemData build() {
            return new DisplayItemData(
                    this.displayName,
                    new ArrayList<>(this.lore),
                    this.customModelData
            );
        }
    }

}
