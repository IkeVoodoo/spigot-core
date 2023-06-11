package me.ikevoodoo.spigotcore.items;

import me.ikevoodoo.spigotcore.items.context.ClickContext;
import me.ikevoodoo.spigotcore.items.context.SetupContext;
import me.ikevoodoo.spigotcore.items.data.ItemData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class Item {

    private static final NamespacedKey ID_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(Item.class), "custom_item_key");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(?<varname>[^}]+)}"); // TODO move this to a more specialized class?
    private final String id;

    @Nullable
    public static String getItemStackId(@NotNull ItemStack stack) {
        Objects.requireNonNull(stack, "Cannot get item id from a null item stack!");

        var meta = stack.getItemMeta();
        Objects.requireNonNull(meta, "Cannot get item id from a null item meta!");

        return meta.getPersistentDataContainer().get(ID_KEY, PersistentDataType.STRING);
    }

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

        var pdc = meta.getPersistentDataContainer();
        pdc.set(ID_KEY, PersistentDataType.STRING, this.getId());

        var variables = new ItemVariables(pdc);

        this.setupItemStack(new SetupContext(variables));

        itemData.apply(meta);

        // TODO move this to a more specialized class?
        var lore = Objects.requireNonNull(meta.getLore());

        lore.replaceAll(input -> VARIABLE_PATTERN.matcher(input).replaceAll(res -> variables.readAsString(res.group(1))));

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
