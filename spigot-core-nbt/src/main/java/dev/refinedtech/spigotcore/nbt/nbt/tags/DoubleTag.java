package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends Tag<Double> {
    public DoubleTag(String name, Double value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeDouble(this.getValue());
    }

    @Override
    public DoubleTag read(DataInput input) throws IOException {
        this.setValue(input.readDouble());
        return this;
    }

}
