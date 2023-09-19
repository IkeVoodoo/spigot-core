package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends Tag<Float> {
    public FloatTag(String name, Float value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeFloat(this.getValue());
    }

    @Override
    public FloatTag read(DataInput input) throws IOException {
        this.setValue(input.readFloat());
        return this;
    }
}
