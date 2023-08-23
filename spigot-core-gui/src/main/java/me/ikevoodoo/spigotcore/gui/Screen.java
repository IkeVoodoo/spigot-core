package me.ikevoodoo.spigotcore.gui;

import me.ikevoodoo.spigotcore.gui.buttons.Button;
import me.ikevoodoo.spigotcore.gui.buttons.ShiftButton;
import me.ikevoodoo.spigotcore.gui.listeners.ScreenListener;
import me.ikevoodoo.spigotcore.gui.pages.PageType;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPage;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPaginator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
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
    private final ScreenListener listener = new ScreenListener(this);
    private boolean deleted;

    public Screen(Plugin plugin, String defaultTitle) {
        this.defaultTitle = defaultTitle;

        this.paginator = new ScreenPaginator();

        this.nextButton = this.shiftPageButton(1);
        this.previousButton = this.shiftPageButton(-1);

        Bukkit.getPluginManager().registerEvents(this.listener, plugin);
    }

    /**
     * Marks the screen as deleted, closes the screen for everyone and unregisters the screen's listener.
     * */
    public void delete() {
        this.deleted = true;
        this.paginator.closeAll();

        HandlerList.unregisterAll(this.listener);
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
        return this.shiftPageButton(amount, null, null);
    }

    public Button shiftPageButton(int amount, ItemStack active, ItemStack inactive) {
        return new ShiftButton(this, amount, active, inactive);
    }

    public boolean isOpen(@NotNull UUID id) {
        return this.openPages.getOrDefault(id, -1) >= 0;
    }

    public void open(@NotNull HumanEntity entity, int pageIndex) {
        this.checkDeleted();

        if (pageIndex >= this.getPageCount()) {
            throw new IllegalArgumentException("Cannot try to open page %s when there are %s page(s)!".formatted(pageIndex, this.getPageCount()));
        }

        this.openPages.put(entity.getUniqueId(), pageIndex);
        this.paginator.openPage(pageIndex, entity);
    }

    public void open(@NotNull HumanEntity entity) {
        this.checkDeleted();

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

        if (Math.abs(page) == 0) {
            this.openPages.remove(entity.getUniqueId());
            return;
        }

        this.openPages.put(entity.getUniqueId(), page);
    }

    public void createPages(@NotNull Consumer<ScreenPage> setup, @NotNull PageType type, int pageCount) {
        this.checkDeleted();

        for (int i = 0; i < pageCount; i++) {
            setup.accept(this.createPage(type));
        }
    }

    public int getPageCount() {
        return this.paginator.getPageCount();
    }

    public ScreenPage createPage(@NotNull PageType type) {
        this.checkDeleted();

        return this.paginator.createPage(this, type, this.defaultTitle);
    }

    public void clear() {
        this.paginator.clear();
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
        if (id == null || id < 0) return null;

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

    private void checkDeleted() {
        if (this.deleted) {
            throw new IllegalStateException("Cannot act on a deleted screen!");
        }
    }
}
