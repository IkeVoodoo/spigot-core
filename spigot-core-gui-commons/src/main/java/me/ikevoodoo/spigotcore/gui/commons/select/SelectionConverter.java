package me.ikevoodoo.spigotcore.gui.commons.select;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface SelectionConverter<T> {

    ItemStack elementToStack(T element);

    Optional<T> stackToElement(ItemStack stack);

}
