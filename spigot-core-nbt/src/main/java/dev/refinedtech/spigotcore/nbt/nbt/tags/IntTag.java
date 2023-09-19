package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends Tag<Integer> {
    public IntTag(String name, Integer value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(this.getValue());
    }

    @Override
    public IntTag read(DataInput input) throws IOException {
        this.setValue(input.readInt());
        return this;
    }
}
