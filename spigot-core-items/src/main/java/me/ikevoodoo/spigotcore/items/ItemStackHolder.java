package me.ikevoodoo.spigotcore.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ItemStackHolder {

    private static final Map<ItemStack, Item> STACK_TO_INSTANCE = new HashMap<>();

    private ItemStackHolder() {
        // Find a good insult
    }

    //region Protected
    static void setInstance(@NotNull ItemStack stack, Item item) {
        if (hasInstance(stack)) {
            throw new IllegalArgumentException("Cannot create an instance for an item stack twice!");
        }

        STACK_TO_INSTANCE.put(stack, item);
    }
    //endregion

    @NotNull
    @ApiStatus.Internal
    public static Item assignInstance(@NotNull ItemStack stack, @NotNull String id) throws IllegalArgumentException {
        ItemRegistry.ensureIdIsRegistered(id);

        if (hasInstance(stack)) {
            throw new IllegalArgumentException("Cannot create an instance for an item stack twice!");
        }

        var item = ItemRegistry.getInstance(id);

        STACK_TO_INSTANCE.put(stack, item);

        return item;
    }

    public static boolean hasInstance(@NotNull ItemStack stack) {
        return STACK_TO_INSTANCE.containsKey(stack);
    }

    @ApiStatus.Internal
    public static void deleteInstance(@NotNull ItemStack stack) {
        STACK_TO_INSTANCE.remove(stack);
    }

    @Nullable
    public static <T extends Item> T getInstance(@NotNull ItemStack stack) {
        try {
            //noinspection unchecked
            return (T) STACK_TO_INSTANCE.get(stack);
        } catch (ClassCastException ignored) {
            return null;
        }
    }

}
