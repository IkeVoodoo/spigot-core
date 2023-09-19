package dev.refinedtech.spigotcore.nbt.nbt.tags;

import dev.refinedtech.spigotcore.nbt.nbt.NBTType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Tag<T> {

    private final String name;
    private final byte type;
    private T value;

    protected Tag(String name, T value) {
        this.name = name;
        this.value = value;

        this.type = NBTType.fromClass(this.getClass()).getId();
    }

    public byte getId() {
        return this.type;
    }

    public final String getName() {
        return name;
    }

    public final T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract void write(DataOutput output) throws IOException;

    public abstract Tag<T> read(DataInput input) throws IOException;

    @Override
    public String toString() {
        return "ByteTag[" +
                "name='" + getName() + '\'' +
                ", value=" + getValue() +
                ']';
    }
}
