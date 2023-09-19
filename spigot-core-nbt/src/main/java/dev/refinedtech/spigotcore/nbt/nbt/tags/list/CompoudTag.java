package dev.refinedtech.spigotcore.nbt.nbt.tags.list;

import dev.refinedtech.spigotcore.nbt.nbt.NBTType;
import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CompoudTag extends Tag<Map<String, Tag<?>>> {
    public CompoudTag(String name, Map<String, Tag<?>> value) {
        super(name, value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for (var tag : this.getValue().values()) {
            output.writeByte(tag.getId());
            output.writeUTF(tag.getName());

            tag.write(output);
        }

        output.writeByte(0);
    }

    @Override
    public CompoudTag read(DataInput input) throws IOException {
        var tags = new LinkedHashMap<String, Tag<?>>();

        byte currentType;
        while ((currentType = input.readByte()) != 0) {
            var nbtType = NBTType.fromId(currentType);

            if (nbtType == null) {
                throw new IllegalStateException("Unknown tag by id: " + currentType + ", is the input malformed?");
            }

            try {
                var tag = nbtType.instantiateEmpty(input.readUTF());
                tag.read(input);

                tags.put(tag.getName(), tag);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Unable to create instance of Tag " + nbtType, e);
            }
        }

        this.setValue(tags);
        return this;
    }
}
