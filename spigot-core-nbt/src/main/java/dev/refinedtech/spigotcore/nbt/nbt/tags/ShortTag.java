package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends Tag<Short> {
    public ShortTag(String name, Short value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeShort(this.getValue());
    }

    @Override
    public ShortTag read(DataInput input) throws IOException {
        this.setValue(input.readShort());
        return this;
    }
}
