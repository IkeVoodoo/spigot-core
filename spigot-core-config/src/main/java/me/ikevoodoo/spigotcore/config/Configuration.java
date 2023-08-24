package me.ikevoodoo.spigotcore.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Comments;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.comments.CommentLine;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.comments.CommentType;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.ikevoodoo.spigotcore.config.annotations.Config;
import me.ikevoodoo.spigotcore.config.annotations.Load;
import me.ikevoodoo.spigotcore.config.annotations.Save;
import me.ikevoodoo.spigotcore.config.annotations.data.CollectionType;
import me.ikevoodoo.spigotcore.config.annotations.data.Getter;
import me.ikevoodoo.spigotcore.config.annotations.data.Setter;
import me.ikevoodoo.spigotcore.config.serialization.ConfigSerializerRegistry;
import me.ikevoodoo.spigotcore.config.serialization.SerializationException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Configuration implements InvocationHandler {

    private static final Map<Class<? extends Annotation>, ConfigAnnotationProcessor<?>> ANNOTATION_PROCESSORS = new HashMap<>();

    static {
        ANNOTATION_PROCESSORS.put(Load.class, (registry, annotation, doc, route, args, def) -> doc.reload());
        ANNOTATION_PROCESSORS.put(Save.class, (registry, annotation, doc, route, args, def) -> doc.save());
        ANNOTATION_PROCESSORS.put(Getter.class, (registry, annotation, doc, route, args, def) -> {
            var res = doc.get(route);
            try {
                if (res instanceof Section section) {
                    res = sectionToMap(registry, section);
                }

                return registry.tryDeserialize(res).orElse(def);
            } catch (SerializationException exception) {
                throw new IllegalStateException("Unable to deserialize '%s'".formatted(route.join('.')), exception);
            }
        });
        ANNOTATION_PROCESSORS.put(Setter.class, (registry, annotation, document, route, params, defaultParam) -> {
            var val = params.length == 0 ? defaultParam : params[0];
            Object value;
            try {
                value = serializeObject(registry, val);
            } catch (IllegalStateException exception) {
                throw new IllegalStateException("Unable to serialize '%s'".formatted(route.join('.')), exception.getCause());
            }

            document.set(route, value == null ? val : value);
            return null;
        });
    }

    private final YamlDocument document;
    private final Map<Method, SettingCache> cache = Collections.synchronizedMap(new HashMap<>());
    private final String name;
    private final ConfigSerializerRegistry registry;

    public static <T> T createConfiguration(Class<T> tClass, File file) throws IOException {
        return createConfiguration(tClass, new ConfigSerializerRegistry(), file);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createConfiguration(Class<T> tClass, ConfigSerializerRegistry registry, File file) throws IOException {
        var save = !file.exists();

        var config = new Configuration(file, registry);
        var proxy = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, config);

        try {
            if (save) {
                config.setDefaults(proxy, tClass);
                config.save();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        return (T) proxy;
    }

    public static String getNameForClass(Class<?> clazz) {
        var annotation = clazz.getAnnotation(Config.class);
        if (annotation == null) {
            return StringUtils.uncapitalize(clazz.getSimpleName());
        }

        var name = annotation.value();

        if (name.isBlank()) {
            return StringUtils.uncapitalize(clazz.getSimpleName());
        }

        return name;
    }

    Configuration(File file, ConfigSerializerRegistry registry) throws IOException {
        this.document = YamlDocument.create(file,
                GeneralSettings.builder()
                        .setUseDefaults(false)
                        .setKeyFormat(GeneralSettings.KeyFormat.OBJECT)
                        .build(),
                LoaderSettings.builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
        );

        this.name = StringUtils.substringBeforeLast(file.getName(), ".");
        this.registry = registry;
    }

    Configuration(YamlDocument document, String name, ConfigSerializerRegistry registry) {
        this.document = document;
        this.name = name;
        this.registry = registry;
    }

    public String getName() {
        return name;
    }

    public void reload() throws IOException {
        this.document.reload();
    }

    public void save() throws IOException {
        this.document.save();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var annotations = method.getAnnotations();

        var anyAnnotationPresent = Arrays.stream(annotations)
                .anyMatch(annotation -> ANNOTATION_PROCESSORS.entrySet().stream().anyMatch(entry -> entry.getKey().isAssignableFrom(annotation.getClass())));

        if (!anyAnnotationPresent) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }

        var cached = this.cache.computeIfAbsent(method, m -> new SettingCache(this.getRouteFor(method), null, null));

        var returnType = method.getReturnType();
        if (returnType.isAnnotationPresent(Config.class)) {
            if (cached.getConfig() == null) {
                cached.setConfig(new Configuration(this.document, Configuration.getNameForClass(returnType), this.registry));
                cached.setProxy(Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[] {
                        returnType
                }, cached.getConfig()));
            }

            return cached.getProxy();
        }

        Object defaultValue = null;

        try {
            defaultValue = InvocationHandler.invokeDefault(proxy, method, args);
        } catch (Exception ignored) {
            // Ignored
        }

        Object result = null;
        for (var annotation : annotations) {
            var processor = getProcessor(annotation);
            if (processor == null) continue;

            var res = processor.process(this.registry, annotation, this.document, cached.getRoute(), args, defaultValue);
            if (res != null) {
                result = res;
            }
        }


        if (result instanceof Collection<?> collection) {
            return validateAndReturnCollection(method.getAnnotation(CollectionType.class), cached.getRoute(), collection);
        }

        if (method.getReturnType() == Optional.class) {
            if (result instanceof Optional<?>) {
                return result;
            }

            return Optional.ofNullable(result);
        }

        return result;
    }

    private Collection<?> validateAndReturnCollection(CollectionType type, Route route, Collection<?> collection) {
        if (type == null) {
            return collection;
        }

        for (var element : collection) {
            if (element != null && element.getClass() != type.value()) {
                throw new IllegalArgumentException("Expected a '%s' in collection '%s', got value '%s' of type '%s'"
                        .formatted(type.value().getSimpleName(), route.join('.'), element, element.getClass().getSimpleName()));
            }
        }

        return collection;
    }

    private ConfigAnnotationProcessor<?> getProcessor(Annotation annotation) {
        for (var entry : ANNOTATION_PROCESSORS.entrySet()) {
            if (entry.getKey().isAssignableFrom(annotation.getClass())) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void setDefaults(Object proxy, Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (var method : clazz.getDeclaredMethods()) {
            var cached = new SettingCache(this.getRouteFor(method), null, null);
            this.cache.put(method, cached);

            if (method.isAnnotationPresent(Getter.class)) {
                this.setGetterDefaults(proxy, method, cached);
            }
        }

        this.setCommentsFor(clazz);
    }

    private void setGetterDefaults(Object proxy, Method method, SettingCache cached) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        var returnType = method.getReturnType();
        if (returnType.isAnnotationPresent(Config.class)) {
            if (cached.getConfig() == null) {
                cached.setConfig(new Configuration(this.document, Configuration.getNameForClass(returnType), this.registry));
                cached.setProxy(Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[] {
                        returnType
                }, cached.getConfig()));
            }

            this.setDefaults(cached.getProxy(), returnType);
            return;
        }

        this.document.set(cached.getRoute(), getSerializedValue(proxy, method, cached.getRoute()));

        var lines = getComments(method);
        this.document.getOptionalBlock(cached.getRoute()).ifPresent(block -> {
            block.removeComments();
            Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, lines);
        });
    }

    private Object getSerializedValue(Object proxy, Method method, Route route, Object... args) throws InvocationTargetException, IllegalAccessException {
        var value = method.invoke(proxy, args);
        try {
            return Optional.ofNullable(serializeObject(this.registry, value)).orElse(value);
        } catch (IllegalStateException exception) {
            throw new IllegalStateException("Unable to serialize '%s'".formatted(route.join('.')), exception.getCause());
        }
    }

    private static Object serializeObject(ConfigSerializerRegistry registry, Object value) {
        Object serialized;
        try {
            serialized = registry.trySerialize(value).orElse(value);

            if (serialized != null && Map.class.isAssignableFrom(serialized.getClass())) {
                var map = (Map<?, ?>) serialized;

                var out = new HashMap<>();
                for (var entry : map.entrySet()) {
                    out.put(serializeObject(registry, entry.getKey()), serializeObject(registry, entry.getValue()));
                }

                serialized = out;
            }
        } catch (SerializationException exception) {
            throw new IllegalStateException(exception);
        }

        return serialized;
    }


    private static Map<Object, Object> sectionToMap(ConfigSerializerRegistry registry, Section section) {
        var out = new HashMap<Object, Object>(section.getStringRouteMappedValues(false));

        for (var entry : out.entrySet()) {
            if (entry.getValue() instanceof Section section1) {
                Object res = sectionToMap(registry, section1);
                try {
                    res = registry.tryDeserialize(res).orElse(res);
                } catch (SerializationException ignored) {
                    // Ignored
                }

                entry.setValue(res);
            }
        }

        return out;
    }

    private void setCommentsFor(Class<?> clazz) {
        var lines = getComments(clazz);
        var route = getRouteFor(clazz);
        if (route.isEmpty()) return;

        this.document.getOptionalBlock(this.createRoute(route)).ifPresent(block -> {
            block.removeComments();
            Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, lines);
        });
    }

    private List<CommentLine> getComments(AnnotatedElement element) {
        var comments = new ArrayList<Optional<String>>();

        for (var annotation : element.getAnnotations()) {
            if (!(annotation instanceof me.ikevoodoo.spigotcore.config.annotations.comments.Comments commentsAnnotation)) {
                continue;
            }

            for (var comment : commentsAnnotation.value()) {
                if(comment.isEmpty()) {
                    comments.add(Optional.empty());
                    continue;
                }

                comments.add(Optional.of(commentsAnnotation.prefix() + comment));
            }
        }

        return comments
                .stream()
                .map(optionalLine ->
                    optionalLine
                            .map(line -> new CommentLine(Optional.empty(), Optional.empty(), line, CommentType.BLOCK))
                            .orElse(Comments.BLANK_LINE))
                .toList();
    }

    private Route getRouteFor(Method method) {
        var paths = getRouteFor(method.getDeclaringClass());

        var getter = method.getAnnotation(Getter.class);
        if (getter != null) {
            paths.add(getter.target().isBlank()
                    ? StringUtils.uncapitalize(method.getName().replaceFirst("get", ""))
                    : getter.target());

            return this.createRoute(paths);
        }

        var setter = method.getAnnotation(Setter.class);
        if (setter != null) {
            paths.add(setter.target().isBlank()
                    ? StringUtils.uncapitalize(method.getName().replaceFirst("set", ""))
                    : setter.target());

            return this.createRoute(paths);
        }

        paths.add(StringUtils.uncapitalize(method.getName()));

        return this.createRoute(paths);
    }

    private List<String> getRouteFor(Class<?> clazz) {
        var paths = new LinkedList<String>();
        var declaringClass = clazz;

        getNameFor(declaringClass).ifPresent(paths::addFirst);

        while ((declaringClass = declaringClass.getDeclaringClass()) != null) {
            getNameFor(declaringClass).ifPresent(paths::add);
        }

        return paths;
    }

    private Route createRoute(List<String> paths) {
        return Route.from(paths.toArray());
    }

    private Optional<String> getNameFor(Class<?> clazz) {
        var cfg = clazz.getAnnotation(Config.class);
        if (cfg == null || cfg.hidden()) return Optional.empty();

        return Optional.of(Configuration.getNameForClass(clazz));
    }

}
