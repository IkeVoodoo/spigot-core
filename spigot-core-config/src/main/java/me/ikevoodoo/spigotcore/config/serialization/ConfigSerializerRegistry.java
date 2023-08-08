package me.ikevoodoo.spigotcore.config.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ConfigSerializerRegistry {

    private final Map<Class<?>, ConfigSerializer<Object, Object>> serializers = new HashMap<>();
    private final Map<Class<?>, ConfigSerializer<Object, Object>> deserializers =  new HashMap<>();

    @SuppressWarnings("unchecked")
    public void register(ConfigSerializer<?, ?> serializer) {
        var cast = (ConfigSerializer<Object, Object>) serializer;

        this.serializers.put(serializer.getOutputType(), cast);
        this.deserializers.put(serializer.getInputType(), cast);
    }

    @SuppressWarnings("unchecked")
    public <I> Optional<I> trySerialize(Object input) throws Throwable {
        if (input == null) return Optional.empty();

        var serializer = this.serializers.get(input.getClass());
        if (serializer == null) return Optional.empty();

        if (!input.getClass().isAssignableFrom(serializer.getOutputType())) return Optional.empty();

        var result = serializer.serialize(serializer.getOutputType().cast(input));
        if (result == null) return Optional.empty();

        return Optional.of((I) result);
    }

    @SuppressWarnings("unchecked")
    public <O> Optional<O> tryDeserialize(Object input) throws Throwable {
        if (input == null) return Optional.empty();

        var serializer = this.deserializers.get(input.getClass());
        if (serializer == null) return Optional.empty();

        if (!input.getClass().isAssignableFrom(serializer.getInputType())) return Optional.empty();

        var result = serializer.deserialize(serializer.getInputType().cast(input));
        if (result == null) return Optional.empty();

        return Optional.of((O) result);
    }

}
