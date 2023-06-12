package me.ikevoodoo.spigotcore.items;

import me.ikevoodoo.spigotcore.items.data.DestructionItemData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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
    public static Item assignInstance(@NotNull ItemStack stack, @NotNull String id) throws IllegalArgumentException {
        ItemRegistry.ensureIdIsRegistered(id);

        if (hasInstance(stack)) {
            throw new IllegalArgumentException("Cannot create an instance for an item stack twice!");
        }

        var item = ItemRegistry.getStatelessOrCreateItem(id);
        STACK_TO_INSTANCE.put(stack, item);

        return item;
    }

    public static boolean hasInstance(@NotNull ItemStack stack) {
        return STACK_TO_INSTANCE.containsKey(stack);
    }

    public static void deleteInstance(@NotNull ItemStack stack) {
        STACK_TO_INSTANCE.remove(stack);
    }

    public static void tryDeleteOld() {
        var iterator = STACK_TO_INSTANCE.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            var stack = entry.getKey();
            var item = entry.getValue();

            var destruction = DestructionItemData.getDestructionData(stack);
            if (!destruction.willDestroy()) continue;

            var createdAt = DestructionItemData.getCreatedAt(stack);

            var ending = createdAt + destruction.destroyAfterMilliseconds();
            if (ending <= System.currentTimeMillis()) {
                // TODO remove the item from inventory, maybe done?
                item.onDestroy(); // TODO add context
                stack.setAmount(0);
                iterator.remove();
            }
        }
    }

    public static int tryDeleteOldEvery(long ticks, Plugin plugin) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ItemStackHolder::tryDeleteOld, ticks, ticks);
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
