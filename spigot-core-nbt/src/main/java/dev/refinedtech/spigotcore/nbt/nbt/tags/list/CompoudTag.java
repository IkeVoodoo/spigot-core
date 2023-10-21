package dev.refinedtech.spigotcore.nbt.nbt.tags.list;

import dev.refinedtech.spigotcore.nbt.NBTTagContainer;
import dev.refinedtech.spigotcore.nbt.nbt.NBTType;
import dev.refinedtech.spigotcore.nbt.nbt.tags.DoubleTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.array.IntArrayTag;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CompoudTag extends Tag<Map<String, Tag<?>>> {
    public CompoudTag(String name, Map<String, Tag<?>> value) {
        super(name, value);
    }

    public NBTTagContainer getAsContainer() {
        return new NBTTagContainer(this);
    }

    public void add(Tag<?> tag) {
        this.getValue().put(tag.getName(), tag);
    }

    public void clear() {
        this.getValue().clear();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Tag<?>> T getTag(String key) {
        return (T) this.getValue().get(key);
    }

    public Vector readDoubleVector(String key) {
        var list = this.<ListTag<DoubleTag>>getTag(key).getValue();

        return new Vector(
                list.get(0).getValue(),
                list.get(1).getValue(),
                list.get(2).getValue()
        );
    }

    public void writeDoubleVector(String key, Vector vector) {
        var list = this.<ListTag<DoubleTag>>getTag(key).getValue();

        list.get(0).setValue(vector.getX());
        list.get(1).setValue(vector.getY());
        list.get(2).setValue(vector.getZ());
    }

    @Nullable
    public UUID readUUID(String key) {
        var arr = this.<IntArrayTag>getTag(key);
        if (arr == null) return null;

        var mostSignificantBits = (((long)arr.get(0)) << 32) | (arr.get(1) & 0xffffffffL);
        var leastSignificantBits = (((long)arr.get(2)) << 32) | (arr.get(3) & 0xffffffffL);

        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public void writeUUID(String key, UUID id) {
        var arr = this.<IntArrayTag>getTag(key);
        if (arr == null) return;

        var most = id.getMostSignificantBits();
        arr.set(0, (int)(most >> 32));
        arr.set(1, (int)(most));

        var least = id.getLeastSignificantBits();
        arr.set(2, (int)(least >> 32));
        arr.set(3, (int)(least));
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for (var tag : this.getValue().values()) {
            output.writeByte(tag.getId());
            output.writeUTF(tag.getName());

            tag.write(output);
        }

        output.writeByte(0);
    }

    @Override
    public CompoudTag read(DataInput input) throws IOException {
        var tags = new LinkedHashMap<String, Tag<?>>();

        byte currentType;
        while ((currentType = input.readByte()) != 0) {
            var nbtType = NBTType.fromId(currentType);

            if (nbtType == null) {
                throw new IllegalStateException("Unknown tag by id: " + currentType + ", is the input malformed?");
            }

            try {
                var tag = nbtType.instantiateEmpty(input.readUTF());
                tag.read(input);

                tags.put(tag.getName(), tag);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Unable to create instance of Tag " + nbtType, e);
            }
        }

        this.setValue(tags);
        return this;
    }
}
