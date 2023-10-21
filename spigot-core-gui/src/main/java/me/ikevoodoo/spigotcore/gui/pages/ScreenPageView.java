package me.ikevoodoo.spigotcore.gui.pages;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScreenPageView {
    private final ScreenPage page;
    private final InventoryView view;
    private final PageButtonHolder buttonHolder;

    public ScreenPageView(ScreenPage page, InventoryView view, PageButtonHolder buttonHolder) {
        this.page = page;
        this.view = view;
        this.buttonHolder = buttonHolder;
    }

    public void setItem(@NotNull PagePosition position, @Nullable ItemStack stack) {
        var slot = this.page.getSlot(position);

        this.view.setItem(slot, stack);
    }

    public PageButtonHolder getButtonHolder() {
        return buttonHolder;
    }

    @ApiStatus.Internal
    protected InventoryView getView() {
        return this.view;
    }
}
