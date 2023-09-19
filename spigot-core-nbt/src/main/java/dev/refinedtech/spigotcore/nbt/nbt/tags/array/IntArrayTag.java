package dev.refinedtech.spigotcore.nbt.nbt.tags.array;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntArrayTag extends ArrayTag<Integer> {
    public IntArrayTag(String name, Integer[] value) {
        super(name, value);
    }

    @Override
    protected void writeElement(DataOutput output, int index) throws IOException {
        output.writeInt(this.get(index));
    }

    @Override
    protected Integer readElement(DataInput input) throws IOException {
        return input.readInt();
    }
}
