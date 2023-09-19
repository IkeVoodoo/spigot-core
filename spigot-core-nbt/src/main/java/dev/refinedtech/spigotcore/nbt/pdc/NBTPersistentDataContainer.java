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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NBTPersistentDataContainer implements PersistentDataContainer {

    private final NBTTagContainer tagContainer;
    private final NBTPersistentDataAdapterContext adapterContext;
    private final Set<NamespacedKey> cachedKeys;
    private final Set<NamespacedKey> cachedKeysView;

    public NBTPersistentDataContainer(NBTTagContainer tagContainer) {
        this.tagContainer = tagContainer;

        this.adapterContext = new NBTPersistentDataAdapterContext(this.tagContainer);


        this.cachedKeys = this.tagContainer.getKeys().stream().map(NamespacedKey::fromString).collect(Collectors.toSet());
        this.cachedKeysView = Collections.unmodifiableSet(this.cachedKeys);
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
        var value = this.tagContainer.getValue(key.toString());
        if (value == null) {
            return false;
        }

        return type.getPrimitiveType().isAssignableFrom(value.getClass());
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T, Z> Z get(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        if (type.getPrimitiveType() == PersistentDataContainer.class) {
            return (Z) new NBTPersistentDataContainer(this.tagContainer.getSection(key.toString()));
        }

        var value = this.tagContainer.getValue(key.toString());
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
        return this.cachedKeysView;
    }

    @Override
    public void remove(@NotNull NamespacedKey key) {
        this.tagContainer.remove(key.toString());
        this.cachedKeys.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return this.cachedKeys.isEmpty();
    }

    @NotNull
    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return this.adapterContext;
    }
}
