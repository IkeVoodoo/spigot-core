package me.ikevoodoo.spigotcore.config.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ConfigSerializerRegistry {

    private final Map<GenericSerializerType<?>, ConfigSerializer<Object, Object>> serializers = new HashMap<>();
    private final Map<GenericSerializerType<?>, ConfigSerializer<Object, Object>> deserializers =  new HashMap<>();

    @SuppressWarnings("unchecked")
    public void register(ConfigSerializer<?, ?> serializer) {
        var cast = (ConfigSerializer<Object, Object>) serializer;

        this.serializers.put(serializer.getOutputType(), cast);
        this.deserializers.put(serializer.getInputType(), cast);
    }

    @SuppressWarnings("unchecked")
    public <I> Optional<I> trySerialize(Object input) throws SerializationException {
        if (input == null) return Optional.empty();

        for (var serializer : findSerializers(input.getClass(), this.serializers)) {
            if (!input.getClass().isAssignableFrom(serializer.getOutputType().clazz())) continue;

            try {
                var result = serializer.serialize(serializer.getOutputType().clazz().cast(input));

                return Optional.ofNullable((I) result);
            } catch (SerializationException ignored) {
                // Ignored
            }

        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <O> Optional<O> tryDeserialize(Object input) throws SerializationException {
        if (input == null) return Optional.empty();

        for (var serializer : findSerializers(input.getClass(), this.deserializers)) {
            if (!serializer.getInputType().clazz().isAssignableFrom(input.getClass())) continue;

            if (input instanceof Map<?,?> map && !mapValuesMatchTypes(map, serializer.getInputType().types())) {
                throw new IllegalArgumentException("Could not deserialize map!");
            }

            try {
                var result = serializer.deserialize(serializer.getInputType().clazz().cast(input));

                return Optional.ofNullable((O) result);
            } catch (SerializationException ignored) {
                // Ignored
            }
        }

        return Optional.empty();
    }

    private boolean mapValuesMatchTypes(Map<?, ?> map, Class<?>... types) {
        if (types.length == 0) return true;

        var i = 0;
        for (var value : map.values()) {
            if (value.getClass() != types[i]) {
                return false;
            }
            i++;
        }

        return true;
    }

    private List<ConfigSerializer<Object, Object>> findSerializers(Class<?> clazz, Map<GenericSerializerType<?>, ConfigSerializer<Object, Object>> map) {
        var out = new ArrayList<ConfigSerializer<Object, Object>>();
        for (var serializer : map.entrySet()) {
            if (serializer.getKey().clazz().isAssignableFrom(clazz)) {
                out.add(serializer.getValue());
            }
        }

        return out;
    }

}
