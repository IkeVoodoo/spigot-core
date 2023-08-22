package me.ikevoodoo.spigotcore.gui;

import me.ikevoodoo.spigotcore.gui.buttons.Button;
import me.ikevoodoo.spigotcore.gui.listeners.ScreenListener;
import me.ikevoodoo.spigotcore.gui.pages.PageType;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPage;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPaginator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Screen {

    private final String defaultTitle;
    private final ScreenPaginator paginator;
    private final Map<UUID, Integer> openPages = new HashMap<>();
    private final Button nextButton;
    private final Button previousButton;

    public Screen(Plugin plugin, String defaultTitle) {
        this.defaultTitle = defaultTitle;

        this.paginator = new ScreenPaginator();

        this.nextButton = this.shiftPageButton(1);
        this.previousButton = this.shiftPageButton(-1);

        Bukkit.getPluginManager().registerEvents(new ScreenListener(this), plugin);
    }

    public boolean next(@NotNull HumanEntity entity) {
        return this.shiftPage(entity, 1);
    }

    public boolean previous(@NotNull HumanEntity entity) {
        return this.shiftPage(entity, -1);
    }

    public boolean shiftPage(@NotNull HumanEntity entity, int offset) {
        var current = this.openPages.get(entity.getUniqueId());
        if (current == null) return false;

        var next = current + offset;
        if (next >= this.getPageCount() || next < 0) return false;

        this.open(entity, next);
        return true;
    }

    public Button nextButton() {
        return this.nextButton;
    }

    public Button previousButton() {
        return this.previousButton;
    }

    public Button shiftPageButton(int amount) {
        return new ShiftButton(this, amount);
    }

    public void open(@NotNull HumanEntity entity, int pageIndex) {
        if (pageIndex >= this.getPageCount()) {
            throw new IllegalArgumentException("Cannot try to open page %s when there are %s page(s)!".formatted(pageIndex, this.getPageCount()));
        }

        this.openPages.put(entity.getUniqueId(), pageIndex);
        this.paginator.openPage(pageIndex, entity);
    }

    public void open(@NotNull HumanEntity entity) {
        var page = this.openPages.compute(entity.getUniqueId(), (id, num) -> Math.min(num == null ? 0 : Math.abs(num), Math.max(this.getPageCount() - 1, 0)));
        this.paginator.openPage(page, entity);
    }

    public void clearClose(@NotNull HumanEntity entity) {
        this.closePage(entity);
        this.openPages.remove(entity.getUniqueId());
    }

    public void close(@NotNull HumanEntity entity) {
        var page = this.closePage(entity);
        if (page == null) return;

        this.openPages.put(entity.getUniqueId(), page);
    }

    public void createPages(@NotNull Consumer<ScreenPage> setup, @NotNull PageType type, int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            setup.accept(this.createPage(type));
        }
    }

    public int getPageCount() {
        return this.paginator.getPageCount();
    }

    public ScreenPage createPage(@NotNull PageType type) {
        return this.paginator.createPage(this, type, this.defaultTitle);
    }

    public void deletePage(int index) {
        this.paginator.deletePage(index);
    }

    public ScreenPage getPage(int index) {
        return this.paginator.getPage(index);
    }

    @Nullable
    public ScreenPage getPageFor(UUID uuid) {
        var id = this.openPages.get(uuid);
        if (id == null) return null;

        return this.getPage(id);
    }

    private Integer closePage(@NotNull HumanEntity entity) {
        var entityId = entity.getUniqueId();
        var page = this.openPages.get(entityId);
        if (page == null || page < 0) {
            return null;
        }

        this.paginator.closePage(page, entityId);

        return -page;
    }

    private record ShiftButton(Screen screen, int amount) implements Button {

        @Override
        public void onClick(SlotEvent event, ItemStack stack, ClickType clickType) {
            this.screen.shiftPage(event.player(), this.amount);
        }

        @Override
        public boolean isActive(SlotEvent event) {
            var next = event.page().index() + this.amount;

            return next < this.screen.getPageCount() && next >= 0;
        }

    }
}
