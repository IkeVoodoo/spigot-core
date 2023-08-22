package me.ikevoodoo.spigotcore.gui.buttons;

import me.ikevoodoo.spigotcore.gui.SlotEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface ClickButton {

    void onClick(SlotEvent event, ItemStack clicked, ClickType type);

}
