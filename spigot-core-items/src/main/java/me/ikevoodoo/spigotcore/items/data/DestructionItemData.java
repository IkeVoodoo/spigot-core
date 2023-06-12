package me.ikevoodoo.spigotcore.items.data;

import me.ikevoodoo.spigotcore.items.Item;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public record DestructionItemData(
        boolean willDestroy,
        long destroyAfterMilliseconds
) {

    private static final NamespacedKey DESTROY_AFTER_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(Item.class), "destroy_after_ticks");
    private static final NamespacedKey CREATED_AT_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(Item.class), "created_at_ticks");

    /**
     * Creates an item destruction data which will destroy the item after X ticks.
     *
     * @param ticks The amount of ticks the item will live for.
     * @since 1.0.0
     *
     * @return New destruction item data.
     * */
    @NotNull
    public static DestructionItemData destroyAfter(long ticks) {
        return new DestructionItemData(true, ticks / 20 * 1000);
    }

    /**
     * Creates an item destruction data which will never destroy the item.
     *
     * @since 1.0.0
     *
     * @return New destruction item data.
     * */
    @NotNull
    public static DestructionItemData neverDestroy() {
        return new DestructionItemData(false, 0);
    }

    /**
     * Creates item destruction data instance from an item stack.
     *
     * @param itemStack The item stack to create the destruction data from, must not be null.
     *                  It's item meta must also not be null.
     * @since 1.1.0
     *
     * @return Either {@link #neverDestroy()} if the item doesn't have the key, or {@link #destroyAfter(long)} if it does.
     * */
    public static DestructionItemData getDestructionData(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "Cannot get destruction data from a null item stack!");

        var itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta, "Cannot get destruction data from a null item meta!");

        var pdc = itemMeta.getPersistentDataContainer();

        if (!pdc.has(DESTROY_AFTER_KEY, PersistentDataType.LONG)) {
            return DestructionItemData.neverDestroy();
        }

        return DestructionItemData.destroyAfter(pdc.getOrDefault(DESTROY_AFTER_KEY, PersistentDataType.LONG, 0L));
    }

    public static long getCreatedAt(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "Cannot get destruction data from a null item stack!");

        var itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta, "Cannot get destruction data from a null item meta!");

        var pdc = itemMeta.getPersistentDataContainer();

        return pdc.getOrDefault(CREATED_AT_KEY, PersistentDataType.LONG, -1L);
    }

    public void apply(@NotNull ItemMeta itemMeta) {
        if(!this.willDestroy()) return;

        Objects.requireNonNull(itemMeta, "Cannot set destruction data on a null item meta!");

        var pdc = itemMeta.getPersistentDataContainer();
        pdc.set(DESTROY_AFTER_KEY, PersistentDataType.LONG, this.destroyAfterMilliseconds());
        pdc.set(CREATED_AT_KEY, PersistentDataType.LONG, System.currentTimeMillis());
    }

}
