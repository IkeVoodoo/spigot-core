package me.ikevoodoo.spigotcore.gui.pages;

import me.ikevoodoo.spigotcore.gui.Screen;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScreenPage {

    private final Screen parentScreen;
    private final PageType type;
    private final int index;
    private final ItemStack[] items;
    private final Map<UUID, ScreenPageView> openInventories = new HashMap<>();
    private final String defaultTitle;
    private final PageButtonHolder pageButtonHolder;
    private String title;

    ScreenPage(Screen parentScreen, PageType type, String title, int index) {
        this.parentScreen = parentScreen;
        this.defaultTitle = title;
        this.title = title;
        this.type = type;
        this.index = index;

        this.items = new ItemStack[this.type.size()];

        this.pageButtonHolder = new PageButtonHolder(this);
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

    public int width() {
        return this.type.width();
    }

    public int height() {
        return this.type.height();
    }

    @Nullable
    public ScreenPageView getView(@Nullable UUID id) {
        return this.openInventories.get(id);
    }

    public void setItem(@NotNull PagePosition position, @Nullable ItemStack stack) {
        var slot = this.getSlot(position);
        for (var inventory : this.openInventories.values()) {
            inventory.getView().setItem(slot, stack);
        }

        this.items[slot] = stack;
    }

    public PagePosition slotPosition(int slot) {
        var x = slot % this.type.width();
        var y = slot / this.type.width();

        return new PagePosition(x, y);
    }

    public PageButtonHolder getPageButtonHolder() {
        return pageButtonHolder;
    }

    protected ScreenPageView open(HumanEntity entity) {
        Inventory inventory;
        if (this.type.size() > 0) {
            inventory = Bukkit.createInventory(null, this.type.size(), this.title);
        } else {
            inventory = Bukkit.createInventory(null, this.type.type(), this.title);
        }

        inventory.setContents(this.items);

        var view = entity.openInventory(inventory);
        assert view != null;

        var screnScreenPageView = new ScreenPageView(this, view, new PageButtonHolder(this.pageButtonHolder));

        this.pageButtonHolder.setItems(this.parentScreen, screnScreenPageView);

        this.openInventories.put(entity.getUniqueId(), screnScreenPageView);

        return screnScreenPageView;
    }

    protected void close(UUID id) {
        var inventory = this.openInventories.remove(id);
        if (inventory == null) return;

        inventory.getView().close();
    }

    protected void closeAll() {
        this.openInventories.forEach((uuid, inventory) -> inventory.getView().close());
        this.openInventories.clear();
    }

    PagePosition limitBounds(PagePosition position) {
        return new PagePosition(getBoundX(position), getBoundY(position));
    }

    public int getSlot(PagePosition position) {
        return Math.abs(getBoundY(position)) * this.type.width() + Math.abs(getBoundX(position));
    }

    private int getBoundX(PagePosition position) {
        return position.x() == Integer.MAX_VALUE ? this.type.width() - 1 : position.x();
    }

    private int getBoundY(PagePosition position) {
        return position.y() == Integer.MAX_VALUE ? this.type.height() - 1 : position.y();
    }
}
