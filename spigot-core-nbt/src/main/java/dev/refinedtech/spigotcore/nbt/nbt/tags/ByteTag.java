package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends Tag<Byte> {

    public ByteTag(String name, Byte value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeByte(this.getValue());
    }

    @Override
    public ByteTag read(DataInput input) throws IOException {
        this.setValue(input.readByte());
        return this;
    }
}
