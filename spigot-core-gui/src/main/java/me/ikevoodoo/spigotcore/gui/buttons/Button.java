package me.ikevoodoo.spigotcore.gui.buttons;

import me.ikevoodoo.spigotcore.gui.SlotEvent;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface Button extends ClickButton {

    default void onInactiveClick(SlotEvent event, ItemStack stack, ClickType type) {

    }

    default ItemStack renderActive(SlotEvent event) {
        return new ItemStack(Material.LIME_DYE);
    }

    default ItemStack renderInactive(SlotEvent event) {
        return new ItemStack(Material.RED_DYE);
    }

    default boolean isActive(SlotEvent event) {
        return true;
    }

}
