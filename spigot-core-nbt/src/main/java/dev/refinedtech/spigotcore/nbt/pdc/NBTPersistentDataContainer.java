package dev.refinedtech.spigotcore.nbt.pdc;

import dev.refinedtech.spigotcore.nbt.NBTTagContainer;
import dev.refinedtech.spigotcore.nbt.nbt.NBTType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NBTPersistentDataContainer implements PersistentDataContainer {

    private final NBTTagContainer tagContainer;
    private final NBTPersistentDataAdapterContext adapterContext;
    private final Map<NamespacedKey, Object> data;
    private final Set<NamespacedKey> keys;

    public NBTPersistentDataContainer(NBTTagContainer tagContainer) {
        this.tagContainer = tagContainer;

        this.adapterContext = new NBTPersistentDataAdapterContext(this.tagContainer);

        this.data = new HashMap<>();

        this.tagContainer.getKeys().stream().map(NamespacedKey::fromString).collect(Collectors.toSet());

        for(var key : this.tagContainer.getKeys()) {
            this.data.put(NamespacedKey.fromString(key), this.tagContainer.getTag(key).getValue());
        }

        this.keys = Collections.unmodifiableSet(this.data.keySet());
    }

    @Override
    public <T, Z> void set(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        var val = type.toPrimitive(value, this.adapterContext);
        var nbt = NBTType.fromPrimitiveType(type.getPrimitiveType());
        if (nbt == null) {
            throw new IllegalStateException("Unable to save type which does not conform to NBT standards!");
        }

        try {
            var tag = nbt.instantiate(key.toString(), val);
            this.tagContainer.setValue(tag.getName(), tag);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to instantiate tag", e);
        }

    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        var tag = this.tagContainer.getTag(key.toString());
        if (tag == null) {
            return false;
        }

        var tagValue = tag.getValue();
        if (tagValue == null) {
            return false;
        }

        return type.getPrimitiveType().isAssignableFrom(tagValue.getClass());
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T, Z> Z get(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        if (type.getPrimitiveType() == PersistentDataContainer.class) {
            return (Z) new NBTPersistentDataContainer(this.tagContainer.getSection(key.toString()));
        }

        var value = this.tagContainer.getTag(key.toString());
        if (value == null) {
            return null;
        }

        return type.fromPrimitive((T) value, this.adapterContext);
    }

    @NotNull
    @Override
    public <T, Z> Z getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultValue) {
        return Optional.ofNullable(this.get(key, type)).orElse(defaultValue);
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getKeys() {
        return this.keys;
    }

    @Override
    public void remove(@NotNull NamespacedKey key) {
        this.tagContainer.remove(key.toString());
        this.data.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @NotNull
    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return this.adapterContext;
    }
}
