package me.ikevoodoo.spigotcore.gui.pages;

import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.SlotEvent;
import me.ikevoodoo.spigotcore.gui.buttons.Button;
import me.ikevoodoo.spigotcore.gui.buttons.ClickButton;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PageButtonHolder {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Map<PagePosition, Button> buttons = new HashMap<>();
    private final Set<ClickButton> clickHandlers = new HashSet<>();

    private final ScreenPage page;

    public PageButtonHolder(ScreenPage page) {
        this.page = page;
    }

    public PageButtonHolder(PageButtonHolder parent) {
        this(parent.page);

        this.buttons.putAll(parent.buttons);
        this.clickHandlers.addAll(parent.clickHandlers);
    }

    public void addButton(PagePosition position, Button button) {
        this.buttons.put(this.page.limitBounds(position), button);
    }

    public void removeButton(PagePosition position) {
        this.buttons.remove(this.page.limitBounds(position));
    }

    public void addClickHandler(ClickButton button) {
        this.clickHandlers.add(button);
    }

    public void removeClickHandler(ClickButton button) {
        this.clickHandlers.remove(button);
    }

    @ApiStatus.Internal
    public void setItems(Screen screen, ScreenPageView view) {
        for (var buttonEntry : this.buttons.entrySet()) {
            var pos = buttonEntry.getKey();
            var button = buttonEntry.getValue();
            var context = new SlotEvent(screen, this.page, view, view.getView().getPlayer(), pos);

            var item = button.isActive(context) ? button.renderActive(context) : button.renderInactive(context);

            view.setItem(pos, item);
        }
    }

    @ApiStatus.Internal
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

}
