package dev.refinedtech.spigotcore.nbt.nbt.tags.array;

import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;
import org.apache.commons.lang.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class ArrayTag<T> extends Tag<T[]> {
    protected ArrayTag(String name, T[] value) {
        super(name, value);
    }

    @Override
    public final void write(DataOutput output) throws IOException {
        output.writeInt(this.size());

        for (int i = 0; i < this.size(); i++) {
            this.writeElement(output, i);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ArrayTag<T> read(DataInput input) throws IOException {
        var array = (T[]) new Object[input.readInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = this.readElement(input);
        }

        this.setValue(array);
        
        return this;
    }

    protected abstract void writeElement(DataOutput output, int index) throws IOException;

    protected abstract T readElement(DataInput input) throws IOException;

    public final int size() {
        return this.getValue().length;
    }

    public final boolean isEmpty() {
        return this.size() == 0;
    }

    public final T get(int index) {
        return this.getValue()[index];
    }

    public final T set(int index, T value) {
        this.getValue()[index] = value;

        return value;
    }

    @SuppressWarnings("unchecked")
    public final void add(int index, T value) {
        this.setValue((T[]) ArrayUtils.add(this.getValue(), index, value));
    }

    @SuppressWarnings("unchecked")
    public final T remove(int index) {
        var prev = this.get(index);
        this.setValue((T[]) ArrayUtils.remove(this.getValue(), index));
        return prev;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        if (this.isEmpty()) return;

        this.setValue((T[]) new Object[1]);
    }


}
