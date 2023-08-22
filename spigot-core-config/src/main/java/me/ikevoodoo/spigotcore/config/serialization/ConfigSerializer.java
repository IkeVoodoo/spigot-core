package me.ikevoodoo.spigotcore.config.serialization;

import java.util.Map;
import java.util.function.Function;

public interface ConfigSerializer<I, O> {

    GenericSerializerType<I> getInputType();
    GenericSerializerType<O> getOutputType();

    O deserialize(I value) throws SerializationException;
    I serialize(O value) throws SerializationException;

    default <T> T parseType(Object obj, Function<String, T> function) {
        return function.apply(String.valueOf(obj));
    }

    default <T> T getFromMap(Map<?, ?> map, String path, T def, Function<String, T> parser) {
        var obj = map.get(path);
        if (obj == null) {
            return def;
        }

        return parseType(obj, parser);
    }


}
