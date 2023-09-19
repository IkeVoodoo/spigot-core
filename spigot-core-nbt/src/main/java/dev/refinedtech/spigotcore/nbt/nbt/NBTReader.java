package dev.refinedtech.spigotcore.nbt.nbt;

import dev.refinedtech.spigotcore.nbt.NBTTagContainer;
import dev.refinedtech.spigotcore.nbt.nbt.tags.list.CompoudTag;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class NBTReader {

    public static NBTTagContainer readNbt(File file) throws IOException {
        try(var is = new FileInputStream(file)) {
            return readNbt(is);
        }
    }

    public static NBTTagContainer readNbt(InputStream stream) throws IOException {
        var dis = wrapInputStream(stream);
        if (dis.readByte() != NBTType.COMPOUND.getId()) {
            throw new IllegalStateException("Unable to read NBT from stream as the first byte isn't a compound!");
        }

        var compound = new CompoudTag(dis.readUTF(), null);
        compound.read(dis);
        return new NBTTagContainer(compound.getName(), compound);
    }

    private static DataInputStream wrapInputStream(InputStream is) throws IOException {
        var input = is instanceof BufferedInputStream bufferedInputStream ? bufferedInputStream : new BufferedInputStream(is);

        return new DataInputStream(switch (getDecoderType(input)) {
            case 1 -> new InflaterInputStream(input);
            case 2 -> new GZIPInputStream(input);
            case 0 -> input;
            default -> throw new IllegalStateException("Illegal state!");
        });
    }

    private static int getDecoderType(InputStream is) throws IOException {
        is.mark(0);
        var read = is.read();
        is.reset();

        if (read == 120) return 1;
        if (read == 31) return 2;

        return 0;
    }

}
