package me.ikevoodoo.spigotcore.gui;

import me.ikevoodoo.spigotcore.gui.pages.PagePosition;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPage;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPageView;
import org.bukkit.entity.HumanEntity;

public final class SlotEvent {

    private final Screen screen;
    private final ScreenPage page;
    private final ScreenPageView view;
    private final HumanEntity player;
    private final PagePosition position;

    private boolean isCancelled;

    public SlotEvent(Screen screen, ScreenPage page, ScreenPageView view, HumanEntity player, PagePosition position) {
        this.screen = screen;
        this.page = page;
        this.view = view;
        this.player = player;
        this.position = position;
    }

    public Screen screen() {
        return screen;
    }

    public ScreenPage page() {
        return page;
    }

    public HumanEntity player() {
        return player;
    }

    public PagePosition position() {
        return position;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}