package me.ikevoodoo.spigotcore.items;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class ItemRegistry {

    private static final Map<Class<? extends Item>, Supplier<? extends Item>> ITEM_SUPPLIERS = new HashMap<>();
    private static final Map<Class<? extends Item>, String> TYPE_TO_ID = new HashMap<>();
    private static final Map<String, Class<? extends Item>> ID_TO_TYPE = new HashMap<>();
    private static final Map<Class<? extends Item>, Item> STATELESS_ITEMS = new HashMap<>();
    private static final Map<ItemStack, Item> STACK_TO_INSTANCE = new HashMap<>();

    private ItemRegistry() {
        throw new IllegalStateException("You can't instantiate a registry you undercooked spaghetti.");
    }

    /**
     * Registers an item if not already registered.
     *
     * @param id The string id, used to persist an item type across restarts.
     * @param type The class of the item
     * @param supplier A supplier for an item instance.
     * @return the registration success, false if the id or type is already registered, otherwise true.
     */
    public static <T extends Item> boolean register(@NotNull String id, @NotNull Class<T> type, @NotNull Supplier<T> supplier) {
        Objects.requireNonNull(id, "Cannot register a null id!");
        Objects.requireNonNull(type, "Cannot register a null type!");
        Objects.requireNonNull(supplier, "Cannot register a null supplier!");

        if (TYPE_TO_ID.containsKey(type) || ID_TO_TYPE.containsKey(id)) return false;

        ITEM_SUPPLIERS.put(type, supplier);
        TYPE_TO_ID.put(type, id);
        ID_TO_TYPE.put(id, type);

        return true;
    }

    @NotNull
    public static <T extends Item> String getId(@NotNull Class<T> type) throws IllegalArgumentException {
        var id = TYPE_TO_ID.get(type);
        if (id == null) {
            throwClassNotRegistered(type); // Exists method here.
        }

        assert id != null;
        return id;
    }

    @NotNull
    public static <T extends Item> Class<T> getType(@NotNull String id) throws IllegalArgumentException {
        var type = ID_TO_TYPE.get(id);
        if (type == null) {
            throwIdNotRegistered(id); // Exists method here.
        }

        assert type != null;

        try {
            //noinspection unchecked
            return (Class<T>) type;
        } catch (ClassCastException ignored) {
            throw new IllegalArgumentException("Id '%s' is of type '%s'!".formatted(id, type.getCanonicalName()));
        }
    }

    @NotNull
    public static Item createInstance(@NotNull ItemStack stack, @NotNull String id) throws IllegalArgumentException {
        ensureIdIsRegistered(id); // Exists if the id isn't registered.

        if (hasInstance(stack)) {
            throw new IllegalArgumentException("Cannot create an instance for an item stack twice!");
        }

        var item = getOrCreateItem(id);
        STACK_TO_INSTANCE.put(stack, item);

        return item;
    }

    public static boolean hasInstance(@NotNull ItemStack stack) {
        return STACK_TO_INSTANCE.containsKey(stack);
    }

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

    //region Protected
    static void registerStatelessInstance(@NotNull Item item) throws IllegalArgumentException {
        if (STATELESS_ITEMS.containsKey(item.getClass())) {
            throw new IllegalArgumentException("Cannot create multiple instances of a stateless item!");
        }

        STATELESS_ITEMS.put(item.getClass(), item);
    }

    static void setInstance(@NotNull ItemStack stack, Item item) {
        if (hasInstance(stack)) {
            throw new IllegalArgumentException("Cannot create an instance for an item stack twice!");
        }

        STACK_TO_INSTANCE.put(stack, item);
    }
    //endregion

    //region Private Utils
    @NotNull
    private static Item getOrCreateItem(@NotNull String id) {
        var type = ensureIdIsRegistered(id);

        var stateless = STATELESS_ITEMS.get(type);
        if (stateless != null) return stateless;

        return ITEM_SUPPLIERS.get(type).get();
    }

    private static void throwClassNotRegistered(@NotNull Class<?> clazz) throws IllegalArgumentException {
        var plugin = JavaPlugin.getProvidingPlugin(ItemRegistry.class);

        throw new IllegalArgumentException("Class '%s' was not registered! Bug the developer%s of '%s' %s".formatted(
                clazz.getCanonicalName(),
                plugin.getDescription().getAuthors().size() == 1 ? "" : "s",
                plugin.getDescription().getFullName(),
                plugin.getDescription().getAuthors()
        ));
    }

    @NotNull
    private static Class<? extends Item> ensureIdIsRegistered(@NotNull String id) throws IllegalArgumentException {
        var type = ID_TO_TYPE.get(id);
        if (type != null) return type;

        throwIdNotRegistered(id);
        return null; // Never called
    }

    private static void throwIdNotRegistered(@NotNull String id) throws IllegalArgumentException {
        var plugin = JavaPlugin.getProvidingPlugin(ItemRegistry.class);

        throw new IllegalArgumentException("Item '%s' was not registered! Bug the developer%s of '%s' %s".formatted(
                id,
                plugin.getDescription().getAuthors().size() == 1 ? "" : "s",
                plugin.getDescription().getFullName(),
                plugin.getDescription().getAuthors()
        ));
    }
    //endregion


}
