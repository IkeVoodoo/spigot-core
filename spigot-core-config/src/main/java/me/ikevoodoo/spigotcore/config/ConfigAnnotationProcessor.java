package me.ikevoodoo.spigotcore.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import me.ikevoodoo.spigotcore.config.serialization.ConfigSerializer;
import me.ikevoodoo.spigotcore.config.serialization.ConfigSerializerRegistry;

import java.io.IOException;
import java.lang.annotation.Annotation;

public interface ConfigAnnotationProcessor<T> {

    T process(ConfigSerializerRegistry registry, Annotation annotation, YamlDocument document, Route route, Object[] params, Object defaultParam) throws IOException;

}
