package dev.refinedtech.spigotcore.nbt.nbt.tags.array;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongArrayTag extends ArrayTag<Long> {
    public LongArrayTag(String name, Long[] value) {
        super(name, value);
    }

    @Override
    protected void writeElement(DataOutput output, int index) throws IOException {
        output.writeLong(this.get(index));
    }

    @Override
    protected Long readElement(DataInput input) throws IOException {
        return input.readLong();
    }
}
