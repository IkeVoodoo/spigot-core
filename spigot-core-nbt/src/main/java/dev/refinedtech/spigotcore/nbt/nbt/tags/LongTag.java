package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends Tag<Long> {
    public LongTag(String name, Long value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeLong(this.getValue());
    }

    @Override
    public LongTag read(DataInput input) throws IOException {
        this.setValue(input.readLong());
        return this;
    }
}
