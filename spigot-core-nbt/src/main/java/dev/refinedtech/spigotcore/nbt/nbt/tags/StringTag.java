package dev.refinedtech.spigotcore.nbt.nbt.tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StringTag extends Tag<String> {
    public StringTag(String name, String value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(this.getValue());
    }

    @Override
    public StringTag read(DataInput input) throws IOException {
        this.setValue(input.readUTF());
        return this;
    }
}
