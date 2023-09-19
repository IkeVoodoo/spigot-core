package dev.refinedtech.spigotcore.nbt;

import dev.refinedtech.spigotcore.nbt.nbt.NBTWriter;
import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.list.CompoudTag;
import dev.refinedtech.spigotcore.nbt.pdc.NBTPersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Set;

public final class NBTTagContainer {

    private final String name;
    private final CompoudTag compoudTag;

    public NBTTagContainer(String name, CompoudTag compoudTag) {
        this.name = name;
        this.compoudTag = compoudTag;
    }

    public String getName() {
        return name;
    }

    public Set<String> getKeys() {
        return this.compoudTag.getValue().keySet();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Tag<?>> T getValue(String key) {
        return (T) this.compoudTag.getValue().get(key);
    }

    @Nullable
    public NBTTagContainer getSection(String key) {
        var value = this.compoudTag.getValue().get(key);
        if (value instanceof CompoudTag tag) {
            return new NBTTagContainer(tag.getName(), tag);
        }

        return null;
    }

    public void setValue(String key, Tag<?> value) {
        this.compoudTag.getValue().put(key, value);
    }

    public void remove(String key) {
        this.compoudTag.getValue().remove(key);
    }

    public void save(File file) throws IOException {
        NBTWriter.writeNbt(file, this.compoudTag);
    }

    public void save(OutputStream outputStream) throws IOException {
        NBTWriter.writeNbt(outputStream, this.compoudTag);
    }

    /**
     * Gets the BukkitValues or PublicBukkitValues from this container.
     * */
    @Nullable
    public NBTPersistentDataContainer asBukkitPersistentDataContainer() {
        var values = Optional.ofNullable(this.getSection("BukkitValues"))
                .orElseGet(() -> this.getSection("PublicBukkitValues"));

        if (values == null) {
            return null;
        }

        return new NBTPersistentDataContainer(values);
    }

}
