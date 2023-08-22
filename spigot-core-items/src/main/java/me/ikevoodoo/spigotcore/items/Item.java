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

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
@SuppressWarnings("unused")
public abstract class Item {

    private static final Random RANDOM = new SecureRandom();
    private static final NamespacedKey ID_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(Item.class), "custom_item_key");
    private static final NamespacedKey RANDOM_NUMBER = new NamespacedKey(JavaPlugin.getProvidingPlugin(Item.class), "custom_item_random");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(?<varname>[^}]+)}");
    private final String id;

    /**
     * Tries to fetch the item id from the stack's persistent data container.
     * The item stack must have a non-null item meta.
     *
     * @param stack The item stack to fetch the id from
     *
     * @since 1.0.0
     *
     * @return the item id if present, otherwise null.
     * */
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

    /**
     * Called whenever the player right-clicks on a block/air/entity.
     *
     * @param context The click context will never be null.
     * @since 1.0.0
     *
     * @see ClickContext
     * */
    public void onRightClick(@NotNull ClickContext context) {
        // No-Op
    }

    /**
     * Called whenever the player left-clicks on a block/air/entity.
     *
     * @param context The click context will never be null.
     * @since 1.0.0
     *
     * @see ClickContext
     * */
    public void onLeftClick(@NotNull ClickContext context) {
        // No-Op
    }

    /**
     * Creates an item stack with the given item data for this item.
     * Stores the id on the generated stack as well as setup variables.
     *
     * @param itemData The item data to use, must not be null.
     * @since 1.0.0
     *
     * @see ItemData
     * */
    @Nullable
    public final ItemStack createItemStack(@NotNull ItemData itemData) {
        Objects.requireNonNull(itemData, "Cannot create item stack with null item data!");

        var stack = new ItemStack(getMaterial());
        var meta = stack.getItemMeta();
        if (meta == null) {
            return null; // Unable to set up the item.
        }

        var pdc = meta.getPersistentDataContainer();
        pdc.set(ID_KEY, PersistentDataType.STRING, this.getId());
        pdc.set(RANDOM_NUMBER, PersistentDataType.INTEGER, RANDOM.nextInt());

        var variables = new ItemVariables(pdc);

        this.setupItemStack(new SetupContext(variables));

        itemData.apply(meta);

        var lore = Objects.requireNonNull(meta.getLore());
        lore.replaceAll(input -> VARIABLE_PATTERN.matcher(input).replaceAll(res -> variables.readAsString(res.group(1))));

        meta.setDisplayName(VARIABLE_PATTERN.matcher(meta.getDisplayName()).replaceAll(res -> variables.readAsString(res.group(1))));

        meta.setLore(lore);
        stack.setItemMeta(meta);

        ItemStackHolder.setInstance(stack, this);

        return stack;
    }

    /**
     * @return the id associated with this item.
     * @since 1.0.0
     * */
    public final String getId() {
        return id;
    }

    /**
     * @return the material to use for this custom item.
     * @since 1.0.0
     * */
    protected abstract Material getMaterial();

    /**
     * Called near the beginning of {@link #createItemStack(ItemData)} to set up variables and the likes.
     *
     * @param context The setup context to use will never be null.
     * @since 1.0.0
     *
     * @see SetupContext
     * @see #createItemStack(ItemData)
     * */
    protected abstract void setupItemStack(SetupContext context);

    /**
     * Does the item change upon use or not? The result of this method should never change.
     *
     * @since 1.0.0
     * */
    protected abstract boolean hasState();

}
