package me.ikevoodoo.spigotcore.gui.pages;

import org.bukkit.event.inventory.InventoryType;

public record PageType(InventoryType type, int size, int width, int height) {

    public static PageType chest(int height) {
        return new PageType(InventoryType.CHEST, 9 * height, 9, height);
    }

}
