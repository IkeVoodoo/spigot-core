package dev.refinedtech.spigotcore.nbt.nbt;

import dev.refinedtech.spigotcore.nbt.nbt.tags.ByteTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.DoubleTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.FloatTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.IntTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.LongTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.ShortTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.StringTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.Tag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.array.ByteArrayTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.array.IntArrayTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.array.LongArrayTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.list.CompoudTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.list.ListTag;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public enum NBTType {

    BYTE(ByteTag.class, Byte.class),
    SHORT(ShortTag.class, Short.class),
    INT(IntTag.class, Integer.class),
    LONG(LongTag.class, Long.class),
    FLOAT(FloatTag.class, Float.class),
    DOUBLE(DoubleTag.class, Double.class),
    BYTE_ARRAY(ByteArrayTag.class, Byte[].class),
    STRING(StringTag.class, String.class),
    LIST(ListTag.class, List.class),
    COMPOUND(CompoudTag.class, CompoudTag.class),
    INT_ARRAY(IntArrayTag.class, Integer[].class),
    LONG_ARRAY(LongArrayTag.class, Long[].class);

    private static final NBTType[] VALUES = values();
    private final Class<? extends Tag> type;
    private final Class<?> primitiveType;
    private Constructor<Tag<?>> constructor;

    @SuppressWarnings("unchecked")
    NBTType(Class<? extends Tag> type, Class<?> primitiveType) {
        this.type = type;
        this.primitiveType = primitiveType;

        for (var cons : type.getConstructors()) {
            if (cons.getParameterCount() == 2 && cons.getParameterTypes()[0] == String.class) {
                this.constructor = (Constructor<Tag<?>>) cons;
            }
        }

        if (this.constructor == null) {
            throw new IllegalStateException("Type " + type + " has no constructor matching default Tag constructor!");
        }
    }

    public byte getId() {
        return (byte) (this.ordinal() + 1);
    }

    public Class<? extends Tag> getType() {
        return type;
    }

    public Class<?> getPrimitiveType() {
        return primitiveType;
    }

    public Tag<?> instantiateEmpty(String name) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.instantiate(name, null);
    }

    public Tag<?> instantiate(String name, Object value) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.constructor.newInstance(name, value);
    }

    public static NBTType fromClass(Class<? extends Tag> type) {
        for (var value : VALUES) {
            if (value.getType() == type) {
                return value;
            }
        }

        return null;
    }

    public static NBTType fromPrimitiveType(Class<?> type) {
        for (var value : VALUES) {
            if (value.getPrimitiveType() == type) {
                return value;
            }
        }

        return null;
    }

    public static NBTType fromId(byte id) {
        var index = id - 1;
        if (index < 0 || index >= VALUES.length) return null;

        return VALUES[index];
    }
}
