package me.ikevoodoo.spigotcore.items;

import me.ikevoodoo.spigotcore.items.context.ClickContext;
import me.ikevoodoo.spigotcore.items.context.SetupContext;
import me.ikevoodoo.spigotcore.items.data.ItemData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class Item {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(?<varname>[^}]+)}"); // TODO move this to a more specialized class?
    private final String id;

    protected Item() {
        this.id = ItemRegistry.getId(this.getClass());

        if (!this.hasState()) {
            ItemRegistry.registerStatelessInstance(this);
        }
    }

    public void onRightClick(ClickContext context) {
        // No-Op
    }

    public void onLeftClick(ClickContext context) {
        // No-Op
    }

    @Nullable
    public final ItemStack createItemStack(ItemData itemData) {
        var stack = new ItemStack(getMaterial());
        var meta = stack.getItemMeta();
        if (meta == null) {
            return null; // Unable to set up the item.
        }

        var variables = new ItemVariables(meta.getPersistentDataContainer());

        this.setupItemStack(new SetupContext(variables));

        itemData.apply(meta);

        // TODO move this to a more specialized class?
        var lore = Objects.requireNonNull(meta.getLore());

        for (int i = 0; i < lore.size(); i++) {
            var matcher = VARIABLE_PATTERN.matcher(lore.get(i));

            lore.set(i, matcher.replaceAll(res -> variables.readAsString(res.group(1))));
        }

        meta.setDisplayName(VARIABLE_PATTERN.matcher(meta.getDisplayName()).replaceAll(res -> variables.readAsString(res.group(1))));

        meta.setLore(lore);
        stack.setItemMeta(meta);

        ItemRegistry.setInstance(stack, this);

        return stack;
    }

    public final String getId() {
        return id;
    }

    protected abstract Material getMaterial();

    protected abstract void setupItemStack(SetupContext context);

    /**
     * Does the item change upon use or not? The result of this method should never change.
     * */
    protected abstract boolean hasState();

}
