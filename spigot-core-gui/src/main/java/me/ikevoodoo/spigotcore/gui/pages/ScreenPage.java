package me.ikevoodoo.spigotcore.gui.pages;

import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.SlotEvent;
import me.ikevoodoo.spigotcore.gui.buttons.Button;
import me.ikevoodoo.spigotcore.gui.buttons.ClickButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScreenPage {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Screen parentScreen;
    private final PageType type;
    private final int index;
    private final ItemStack[] items;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<PagePosition, Button> buttons = new HashMap<>();
    private final Set<ClickButton> clickHandlers = new HashSet<>();
    private final String defaultTitle;
    private String title;

    ScreenPage(Screen parentScreen, PageType type, String title, int index) {
        this.parentScreen = parentScreen;
        this.defaultTitle = title;
        this.title = title;
        this.type = type;
        this.index = index;

        this.items = new ItemStack[this.type.size()];
    }

    public String title() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title == null ? this.defaultTitle : title;
    }

    public int index() {
        return this.index;
    }

    public int size() {
        return this.type.size();
    }

    public void addButton(PagePosition position, Button button) {
        this.buttons.put(this.limitBounds(position), button);
    }

    public void removeButton(PagePosition position) {
        this.buttons.remove(this.limitBounds(position));
    }

    public void setItem(@NotNull PagePosition position, @NotNull ItemStack stack) {
        var slot = this.getSlot(position);
        for (var inventory : this.openInventories.values()) {
            inventory.setItem(slot, stack);
        }

        this.items[slot] = stack;
    }

    public void addClickHandler(ClickButton button) {
        this.clickHandlers.add(button);
    }

    public void removeClickHandler(ClickButton button) {
        this.clickHandlers.remove(button);
    }

    public PagePosition slotPosition(int slot) {
        var x = slot % this.type.width();
        var y = slot / this.type.width();

        return new PagePosition(x, y);
    }

    protected void open(HumanEntity entity) {
        Inventory inventory;
        if (this.type.size() > 0) {
            inventory = Bukkit.createInventory(null, this.type.size(), this.title);
        } else {
            inventory = Bukkit.createInventory(null, this.type.type(), this.title);
        }

        inventory.setContents(this.items);

        var view = entity.openInventory(inventory);
        assert view != null;

        for (var buttonEntry : this.buttons.entrySet()) {
            var pos = buttonEntry.getKey();
            var button = buttonEntry.getValue();
            var context = new SlotEvent(this.parentScreen, this, entity, pos);

            var item = button.isActive(context) ? button.renderActive(context) : button.renderInactive(context);

            view.setItem(this.getSlot(pos), item);
        }

        this.openInventories.put(entity.getUniqueId(), inventory);
    }

    public void fireClickEvent(SlotEvent event, ItemStack stack, ClickType type) {
        var button = this.buttons.get(event.position());
        if (button == null) {
            for (var clickButton : this.clickHandlers) {
                clickButton.onClick(event, stack == null ? AIR.clone() : stack, type);
            }
            return;
        }

        if (!button.isActive(event)) {
            button.onInactiveClick(event, stack == null ? AIR.clone() : stack, type);
            return;
        }

        button.onClick(event, stack == null ? AIR.clone() : stack, type);
    }

    protected void close(UUID id) {
        var inventory = this.openInventories.remove(id);
        if (inventory == null) return;

        this.closeInventory(inventory);
    }

    protected void closeAll() {
        this.openInventories.forEach((uuid, inventory) -> this.closeInventory(inventory));
        this.openInventories.clear();
    }

    private void closeInventory(Inventory inventory) {
        var viewers = inventory.getViewers();
        for (int i = viewers.size() - 1; i >= 0; i--) {
            viewers.get(i).closeInventory();
        }
    }

    private PagePosition limitBounds(PagePosition position) {
        return new PagePosition(getBoundX(position), getBoundY(position));
    }

    private int getSlot(PagePosition position) {
        return Math.abs(getBoundY(position)) * this.type.width() + Math.abs(getBoundX(position));
    }

    private int getBoundX(PagePosition position) {
        return position.x() == Integer.MAX_VALUE ? this.type.width() - 1 : position.x();
    }

    private int getBoundY(PagePosition position) {
        return position.y() == Integer.MAX_VALUE ? this.type.height() - 1 : position.y();
    }
}
