package me.ikevoodoo.spigotcore.gui.buttons;

import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.SlotEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public record ShiftButton(Screen screen, int amount, ItemStack activeStack, ItemStack inactiveStack) implements Button {

    @Override
    public void onClick(SlotEvent event, ItemStack stack, ClickType clickType) {
        this.screen.shiftPage(event.player(), this.amount);
    }

    @Override
    public boolean isActive(SlotEvent event) {
        var next = event.page().index() + this.amount;

        return next < this.screen.getPageCount() && next >= 0;
    }

    @Override
    public ItemStack renderActive(SlotEvent event) {
        return this.activeStack == null ? Button.super.renderActive(event) : this.activeStack;
    }

    @Override
    public ItemStack renderInactive(SlotEvent event) {
        return this.inactiveStack == null ? Button.super.renderInactive(event) : this.inactiveStack;
    }
}