package me.ikevoodoo.spigotcore.gui.pages;

import me.ikevoodoo.spigotcore.gui.Screen;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScreenPaginator {

    private final List<ScreenPage> pages = new ArrayList<>();

    public ScreenPage createPage(Screen screen, PageType type, String title) {
        var page = new ScreenPage(screen, type, title, this.pages.size());
        this.pages.add(page);
        return page;
    }

    public void deletePage(int index) {
        var page = this.getPage(index);
        page.closeAll();

        this.pages.remove(index);
    }

    public ScreenPage getPage(int index) {
        return this.pages.get(index);
    }

    public void closePage(int index, UUID id) {
        this.getPage(index).close(id);
    }

    public void openPage(int index, HumanEntity entity) {
        this.getPage(index).open(entity);
    }

    public int getPageCount() {
        return this.pages.size();
    }

}
