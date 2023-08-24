package me.ikevoodoo.spigotcore.items;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
@SuppressWarnings("unused")
public final class ItemRegistry {

    private static final Plugin PROVIDING_PLUGIN = JavaPlugin.getProvidingPlugin(ItemRegistry.class);

    private static final Map<Class<? extends Item>, Supplier<? extends Item>> ITEM_SUPPLIERS = new HashMap<>();
    private static final Map<Class<? extends Item>, String> TYPE_TO_ID = new HashMap<>();
    private static final Map<String, Class<? extends Item>> ID_TO_TYPE = new HashMap<>();
    private static final Map<Class<? extends Item>, Item> STATELESS_ITEMS = new HashMap<>();

    private ItemRegistry() {
        throw new IllegalStateException("You can't instantiate a registry you undercooked spaghetti.");
    }

    /**
     * Registers an item if not already registered.
     *
     * @param id The string id, used to persist an item type across restarts.
     * @param type The class of the item
     * @param supplier A supplier for an item instance.
     *
     * @since 1.0.0
     */
    public static <T extends Item> void register(@NotNull String id, @NotNull Class<T> type, @NotNull Supplier<T> supplier) {
        Objects.requireNonNull(id, "Cannot register a null id!");
        Objects.requireNonNull(type, "Cannot register a null type!");
        Objects.requireNonNull(supplier, "Cannot register a null supplier!");

        ITEM_SUPPLIERS.putIfAbsent(type, supplier);
        TYPE_TO_ID.putIfAbsent(type, id);
        ID_TO_TYPE.putIfAbsent(id, type);
    }

    @NotNull
    public static <T extends Item> String getId(@NotNull Class<T> type) throws IllegalArgumentException {
        return Optional.ofNullable(TYPE_TO_ID.get(type)).orElseThrow();
    }

    @NotNull
    public static <T extends Item> Class<T> getType(@NotNull String id) throws IllegalArgumentException {
        var type = Optional.ofNullable(ID_TO_TYPE.get(id)).orElseThrow();

        try {
            //noinspection unchecked
            return (Class<T>) type;
        } catch (ClassCastException ignored) {
            throw new IllegalArgumentException("Id '%s' is of type '%s'!".formatted(id, type.getCanonicalName()));
        }
    }

    @NotNull
    public static Class<? extends Item> ensureIdIsRegistered(@NotNull String id) throws IllegalArgumentException {
        return Optional.ofNullable(ID_TO_TYPE.get(id)).orElseThrow();
    }

    //region Protected
    static void registerStatelessInstance(@NotNull Item item) throws IllegalArgumentException {
        if (STATELESS_ITEMS.containsKey(item.getClass())) {
            throw new IllegalArgumentException("Cannot create multiple instances of a stateless item!");
        }

        STATELESS_ITEMS.put(item.getClass(), item);
    }

    @NotNull
    static Item getStatelessOrCreateItem(@NotNull String id) {
        var type = ensureIdIsRegistered(id);
        return Optional.ofNullable(STATELESS_ITEMS.get(type)).orElseGet(() -> ITEM_SUPPLIERS.get(type).get());
    }
    //endregion


}
