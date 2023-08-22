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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScreenPage {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Screen parentScreen;
    private final Inventory inventory;
    private final PageType type;
    private final int index;
    private final Map<UUID, InventoryView> openInventories = new HashMap<>();
    private final Map<PagePosition, Button> buttons = new HashMap<>();
    private final Set<ClickButton> clickHandlers = new HashSet<>();

    ScreenPage(Screen parentScreen, PageType type, String title, int index) {
        this.parentScreen = parentScreen;

        if (type.size() > 0) {
            this.inventory = Bukkit.createInventory(null, type.size(), title);
        } else {
            this.inventory = Bukkit.createInventory(null, type.type(), title);
        }

        this.type = type;
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    public int size() {
        return this.inventory.getSize();
    }

    public void addButton(PagePosition position, Button button) {
        this.buttons.put(this.limitBounds(position), button);
    }

    public void removeButton(PagePosition position) {
        this.buttons.remove(this.limitBounds(position));
    }

    public void setItem(@NotNull PagePosition position, @NotNull ItemStack stack) {
        this.inventory.setItem(this.getSlot(position), stack);
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
        var view = entity.openInventory(this.inventory);
        assert view != null;

        for (var buttonEntry : this.buttons.entrySet()) {
            var pos = buttonEntry.getKey();
            var button = buttonEntry.getValue();
            var context = new SlotEvent(this.parentScreen, this, entity, pos);

            var item = button.isActive(context) ? button.renderActive(context) : button.renderInactive(context);

            view.setItem(this.getSlot(pos), item);
        }

        this.openInventories.put(entity.getUniqueId(), view);
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
        var page = this.openInventories.remove(id);
        if (page == null) return;

        page.close();
    }

    protected void closeAll() {
        this.openInventories.forEach((uuid, inventoryView) -> inventoryView.close());
        this.openInventories.clear();
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
