package me.ikevoodoo.spigotcore.config.serialization;

import java.util.Arrays;
import java.util.Objects;

public record GenericSerializerType<T>(Class<T> clazz, Class<?>... types) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericSerializerType<?> that = (GenericSerializerType<?>) o;
        return Objects.equals(clazz, that.clazz) && Arrays.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(clazz);
        result = 31 * result + Arrays.hashCode(types);
        return result;
    }

    @Override
    public String toString() {
        return "GenericSerializerType[" +
                "clazz=" + clazz +
                ", types=" + Arrays.toString(types) +
                ']';
    }

}
