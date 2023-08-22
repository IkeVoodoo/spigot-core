package me.ikevoodoo.spigotcore.gui.listeners;

import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.SlotEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class ScreenListener implements Listener {

    private final Screen screen;

    public ScreenListener(Screen screen) {
        this.screen = screen;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        var slot = event.getRawSlot();
        if (slot == -999) return; // The player clicked outside the screen.

        var click = event.getClick();

        if (click.isCreativeAction() || click == ClickType.DOUBLE_CLICK || click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            return;
        }

        var page = this.screen.getPageFor(event.getWhoClicked().getUniqueId());
        if (page == null) {
            return;
        }

        event.setCancelled(true);

        if (slot >= page.size()) {
            return;
        }

        var slotEvent = new SlotEvent(
                this.screen,
                page,
                event.getWhoClicked(),
                page.slotPosition(slot)
        );

        slotEvent.setCancelled(true); // Default cancelled

        page.fireClickEvent(slotEvent, event.getCurrentItem(), click);

        event.setCancelled(slotEvent.isCancelled());
    }

}
