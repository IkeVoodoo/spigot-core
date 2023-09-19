package dev.refinedtech.spigotcore.nbt.nbt.tags.array;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteArrayTag extends ArrayTag<Double> {
    public ByteArrayTag(String name, Double[] value) {
        super(name, value);
    }

    @Override
    protected void writeElement(DataOutput output, int index) throws IOException {
        output.writeDouble(this.get(index));
    }

    @Override
    protected Double readElement(DataInput input) throws IOException {
        return input.readDouble();
    }
}
