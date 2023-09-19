package dev.refinedtech.spigotcore.nbt.nbt;

import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class NBTWriter {

    public static void writeNbt(File file, Tag<?> tag) throws IOException {
        try(var os = new BufferedOutputStream(new FileOutputStream(file))) {
            writeNbt(os, tag);
        }
    }

    public static void writeNbt(OutputStream stream, Tag<?> tag) throws IOException {
        tag.write(new DataOutputStream(stream));
    }

}
