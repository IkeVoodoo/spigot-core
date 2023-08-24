package me.ikevoodoo.spigotcore.gui.commons.select;

import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.pages.PagePosition;
import me.ikevoodoo.spigotcore.gui.pages.PageType;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class ItemSelector<T> {

    private final Screen screen;
    private final Map<UUID, BiConsumer<HumanEntity, T>> choosing = new HashMap<>();

    private final ItemStack next;
    private final ItemStack prev;
    private final ItemStack filler;

    public ItemSelector(@NotNull Plugin plugin, @NotNull String title, @Nullable ItemStack next, @Nullable ItemStack prev, @Nullable ItemStack filler) {
        this.screen = new Screen(plugin, title);

        this.next = next;
        this.prev = prev;
        this.filler = filler;
    }

    public ItemSelector(@NotNull Plugin plugin, @NotNull String title) {
        this(plugin, title, null, null, null);
    }

    public void openFor(@NotNull HumanEntity player, BiConsumer<HumanEntity, T> consumer) {
        if (this.screen.isOpen(player.getUniqueId())) {
            return;
        }

        this.choosing.put(player.getUniqueId(), consumer);
        this.screen.open(player);
    }

    public void setupPages(List<T> items, SelectionConverter<T> converter) {
        this.screen.clear();
        var count = items.size();

        if (count == 0) {
            this.setupPageDisplay(this.screen.createPage(PageType.chest(6)), 1);
            return;
        }

        var perPage = Math.min(9 * 5, count);

        var pageCount = (int) Math.ceil((double) count / perPage);

        this.screen.createPages(page -> {
            this.setupPageDisplay(page, pageCount);

            var offset = page.index() * perPage;
            var total = Math.min(count - offset, perPage);
            for (int i = 0; i < total; i++) {
                var player = items.get(offset + i);

                page.setItem(page.slotPosition(i), converter.elementToStack(player));
            }

            page.addClickHandler((event, stack, type) -> {
                var callback = choosing.get(event.player().getUniqueId());
                if (callback == null) return;

                var optionalElement = converter.stackToElement(stack);
                if (optionalElement.isEmpty()) return;

                event.screen().close(event.player());

                callback.accept(event.player(), optionalElement.get());
            });
        }, PageType.chest(6), pageCount);
    }

    private void setupPageDisplay(@NotNull ScreenPage page, int totalPages) {
        page.addButton(PagePosition.bottomLeft(), this.screen.shiftPageButton(-1, this.prev, this.filler));
        page.addButton(PagePosition.bottomRight(), this.screen.shiftPageButton(1, this.next, this.filler));

        for (int i = 0; i < 9; i++) {
            page.setItem(new PagePosition(i, 5), this.filler);
        }

        page.setTitle(page.title()
                .replace("%page_index%", (page.index() + 1) + "")
                .replace("%page_count%", totalPages + "")
        );
    }

}
