package me.ikevoodoo.spigotcore.items;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ItemVariables {

    private static final Plugin PROVIDING_PLUGIN = JavaPlugin.getProvidingPlugin(ItemVariables.class);
    private static final PersistentDataType<Object, String> TO_STRING = new PersistentDataType<>() {
        @NotNull
        @Override
        public Class<Object> getPrimitiveType() {
            return Object.class;
        }

        @NotNull
        @Override
        public Class<String> getComplexType() {
            return String.class;
        }

        @NotNull
        @Override
        public Object toPrimitive(@NotNull String complex, @NotNull PersistentDataAdapterContext context) {
            return complex;
        }

        @NotNull
        @Override
        public String fromPrimitive(@NotNull Object primitive, @NotNull PersistentDataAdapterContext context) {
            return primitive instanceof Object[] arr ? Arrays.toString(arr) : primitive.toString();
        }
    };

    private final PersistentDataContainer container;

    public ItemVariables(PersistentDataContainer container) {
        this.container = container;
    }

    public void remove(@NotNull String key) {
        this.container.remove(new NamespacedKey(PROVIDING_PLUGIN, key));
    }

    public <T, Z> void set(@NotNull String key, @NotNull PersistentDataType<T, Z> type, @NotNull Z object) {
        this.container.set(new NamespacedKey(PROVIDING_PLUGIN, key), type, object);
    }

    public <T, Z> Z get(@NotNull String key, @NotNull PersistentDataType<T, Z> type) {
        return this.container.get(new NamespacedKey(PROVIDING_PLUGIN, key), type);
    }

    public <T, Z> Z get(@NotNull String key, @NotNull PersistentDataType<T, Z> type, @NotNull Z def) {
        return this.container.getOrDefault(new NamespacedKey(PROVIDING_PLUGIN, key), type, def);
    }

    // CONVENIENCE METHODS \\

    public void setByte(@NotNull String key, @NotNull Byte value) {
        this.set(key, PersistentDataType.BYTE, value);
    }

    @Nullable
    public Byte getByte(@NotNull String key) {
        return this.get(key, PersistentDataType.BYTE);
    }

    public Byte getByte(@NotNull String key, Byte def) {
        return this.get(key, PersistentDataType.BYTE, def);
    }
    
    public void setShort(@NotNull String key, @NotNull Short value) {
        this.set(key, PersistentDataType.SHORT, value);
    }

    @Nullable
    public Short getShort(@NotNull String key) {
        return this.get(key, PersistentDataType.SHORT);
    }

    public Short getShort(@NotNull String key, Short def) {
        return this.get(key, PersistentDataType.SHORT, def);
    }

    public void setInt(@NotNull String key, @NotNull Integer value) {
        this.set(key, PersistentDataType.INTEGER, value);
    }

    @Nullable
    public Integer getInt(@NotNull String key) {
        return this.get(key, PersistentDataType.INTEGER);
    }

    public Integer getInt(@NotNull String key, Integer def) {
        return this.get(key, PersistentDataType.INTEGER, def);
    }

    public void setLong(@NotNull String key, @NotNull Long value) {
        this.set(key, PersistentDataType.LONG, value);
    }

    @Nullable
    public Long getLong(@NotNull String key) {
        return this.get(key, PersistentDataType.LONG);
    }

    public Long getLong(@NotNull String key, Long def) {
        return this.get(key, PersistentDataType.LONG, def);
    }

    public void setFloat(@NotNull String key, @NotNull Float value) {
        this.set(key, PersistentDataType.FLOAT, value);
    }

    @Nullable
    public Float getFloat(@NotNull String key) {
        return this.get(key, PersistentDataType.FLOAT);
    }

    public Float getFloat(@NotNull String key, Float def) {
        return this.get(key, PersistentDataType.FLOAT, def);
    }

    public void setDouble(@NotNull String key, @NotNull Double value) {
        this.set(key, PersistentDataType.DOUBLE, value);
    }

    @Nullable
    public Double getDouble(@NotNull String key) {
        return this.get(key, PersistentDataType.DOUBLE);
    }

    public Double getDouble(@NotNull String key, Double def) {
        return this.get(key, PersistentDataType.DOUBLE, def);
    }

    public void setString(@NotNull String key, @NotNull String value) {
        this.set(key, PersistentDataType.STRING, value);
    }

    @Nullable
    public String getString(@NotNull String key) {
        return this.get(key, PersistentDataType.STRING);
    }

    public String getString(@NotNull String key, String def) {
        return this.get(key, PersistentDataType.STRING, def);
    }

    /**
     * In contrast to {@link #getString(String)} this method uses a special type to make sure everything gets #toString called on
     * */
    public String readAsString(@NotNull String key) {
        return this.get(key, TO_STRING);
    }
}
