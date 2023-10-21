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

public final class NBTTagContainer extends CompoudTag {

    public NBTTagContainer(CompoudTag compoudTag) {
        super(compoudTag.getName(), compoudTag.getValue());
    }

    public Set<String> getKeys() {
        return this.getValue().keySet();
    }

    public boolean hasTag(String key) {
        return this.getValue().containsKey(key);
    }

    @Nullable
    public NBTTagContainer getSection(String key) {
        var value = this.getValue().get(key);
        if (value instanceof CompoudTag tag) {
            return new NBTTagContainer(tag);
        }

        return null;
    }

    public void setValue(String key, Tag<?> value) {
        this.getValue().put(key, value);
    }

    public void remove(String key) {
        this.getValue().remove(key);
    }

    public void save(File file) throws IOException {
        NBTWriter.writeNbt(file, this);
    }

    public void save(OutputStream outputStream) throws IOException {
        NBTWriter.writeNbt(outputStream, this);
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

    @Override
    public String toString() {
        return this.getValue().toString();
    }
}
