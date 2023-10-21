package dev.refinedtech.spigotcore.nbt.nbt.tags.list;

import dev.refinedtech.spigotcore.nbt.nbt.NBTType;
import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ListTag<T extends Tag<?>> extends Tag<List<T>> implements Iterable<T> {

    private byte listType;

    public ListTag(String name, List<T> value) {
        super(name, value);

        if (value != null) {
            this.listType = value.isEmpty() ? 0 : value.get(0).getId();
        }
    }

    public byte getListType() {
        return listType;
    }

    @Override
    public void setValue(List<T> value) {
        this.listType = value.isEmpty() ? 0 : value.get(0).getId();

        super.setValue(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeByte(this.listType);
        output.writeInt(this.getValue().size());

        for (var val : this.getValue()) {
            val.write(output);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListTag<T> read(DataInput input) throws IOException {
        var type = input.readByte();
        var length = input.readInt();

        var tags = new ArrayList<T>();

        for (int i = 0; i < length; i++) {
            var nbtType = NBTType.fromId(type);

            if (nbtType == null) {
                throw new IllegalStateException("Unknown tag by id: " + type + ", is the input malformed?");
            }

            try {
                var tag = nbtType.instantiateEmpty(null);

                tag.read(input);

                tags.add((T) tag);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Unable to create instance of Tag " + nbtType, e);
            }
        }

        this.listType = type;
        this.setValue(tags);
        return this;
    }


    @Override
    public String toString() {
        return "ListTag[" +
                "listType=" + NBTType.fromId(this.listType) +
                ", name='" + getName() + '\'' +
                ", value=" + getValue() +
                ']';
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        var value = this.getValue();
        if (value == null) return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new NoSuchElementException();
            }
        };

        return value.iterator();
    }
}
