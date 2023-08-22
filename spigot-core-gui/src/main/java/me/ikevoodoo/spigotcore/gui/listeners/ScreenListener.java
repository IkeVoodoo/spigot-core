package me.ikevoodoo.spigotcore.gui.listeners;

import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.SlotEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

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

        var shouldCancel = this.handleSlotClick(event.getWhoClicked(), slot, event.getCurrentItem(), click);
        event.setCancelled(shouldCancel);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        var page = this.screen.getPageFor(event.getWhoClicked().getUniqueId());
        if (page == null) {
            return;
        }

        for (var entry : event.getNewItems().entrySet()) {
            var rawSlot = entry.getKey();
            var item = entry.getValue();

            var shouldCancel = this.handleSlotClick(event.getWhoClicked(), rawSlot, item, event.getType() == DragType.SINGLE ? ClickType.RIGHT : ClickType.LEFT);

            if (shouldCancel) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.screen.close(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!this.screen.isOpen(event.getPlayer().getUniqueId())) return;

        event.setCancelled(true);
    }

    private boolean handleSlotClick(HumanEntity clicker, int rawSlot, ItemStack currentStack, ClickType clickType) {
        var page = this.screen.getPageFor(clicker.getUniqueId());
        if (page == null) {
            return false;
        }

        if (rawSlot >= page.size()) {
            return false; // Don't cancel if the event is in the player's inventory.
        }

        var slotEvent = new SlotEvent(
                this.screen,
                page,
                clicker,
                page.slotPosition(rawSlot)
        );

        slotEvent.setCancelled(true); // Default cancelled

        page.fireClickEvent(slotEvent, currentStack, clickType);

        return slotEvent.isCancelled();
    }
}
