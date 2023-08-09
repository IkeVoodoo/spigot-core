package me.ikevoodoo.spigotcore.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Comments;
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
import me.ikevoodoo.spigotcore.config.annotations.comments.Comment;
import me.ikevoodoo.spigotcore.config.annotations.data.CollectionType;
import me.ikevoodoo.spigotcore.config.annotations.data.Getter;
import me.ikevoodoo.spigotcore.config.annotations.data.Setter;
import me.ikevoodoo.spigotcore.config.serialization.ConfigSerializerRegistry;
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
                var value = registry.tryDeserialize(res).orElse(null);
                if (value != null) {
                    res = value;
                }
            } catch (Throwable throwable) {
                throw new IllegalStateException("Unable to deserialize '%s'".formatted(route.join('.')), throwable);
            }

            if (res == null) return def;

            return res;
        });
        ANNOTATION_PROCESSORS.put(Setter.class, (registry, annotation, document, route, params, defaultParam) -> {
            var val = params.length == 0 ? defaultParam : params[0];
            Object value;
            try {
                value = registry.trySerialize(val).orElse(null);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Unable to serialize '%s'".formatted(route.join('.')), throwable);
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
            throw new RuntimeException(e);
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

        stop:
        {
            for (var annotation : annotations) {
                for (var entry : ANNOTATION_PROCESSORS.entrySet()) {
                    if (entry.getKey().isAssignableFrom(annotation.getClass())) {
                        break stop;
                    }
                }
            }

            return InvocationHandler.invokeDefault(proxy, method, args);
        }

        var cached = this.cache.get(method);
        if (cached == null) {
            cached = new SettingCache(this.getRouteFor(method), null, null);
            this.cache.put(method, cached);
        }

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
            // Ignore
        }

        Object result = null;
        for (var annotation : annotations) {

            ConfigAnnotationProcessor<?> processor = null;

            for (var entry : ANNOTATION_PROCESSORS.entrySet()) {
                if (entry.getKey().isAssignableFrom(annotation.getClass())) {
                    processor = entry.getValue();
                    break;
                }
            }

            if (processor == null) continue;

            var res = processor.process(this.registry, annotation, this.document, cached.getRoute(), args, defaultValue);
            if (res != null) {
                result = res;
            }
        }


        if (result instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return collection;
            }

            var collectionType = method.getAnnotation(CollectionType.class);
            if (collectionType == null) {
                return collection;
            }

            for (var element : collection) {
                if (element != null && element.getClass() != collectionType.value()) {
                    throw new IllegalArgumentException("Expected a '%s' in collection '%s', got value '%s' of type '%s'"
                            .formatted(collectionType.value().getSimpleName(), cached.getRoute().join('.'), element, element.getClass().getSimpleName()));
                }
            }

            return collection;
        }

        if (method.getReturnType() == Optional.class) {
            if (result instanceof Optional<?>) {
                return result;
            }

            return Optional.ofNullable(result);
        }

        return result;
    }

    private void setDefaults(Object proxy, Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (var method : clazz.getDeclaredMethods()) {
            var cached = new SettingCache(this.getRouteFor(method), null, null);
            this.cache.put(method, cached);

            if (method.isAnnotationPresent(Getter.class)) {
                var returnType = method.getReturnType();
                if (returnType.isAnnotationPresent(Config.class)) {
                    if (cached.getConfig() == null) {
                        cached.setConfig(new Configuration(this.document, Configuration.getNameForClass(returnType), this.registry));
                        cached.setProxy(Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[] {
                                returnType
                        }, cached.getConfig()));
                    }

                    this.setDefaults(cached.getProxy(), returnType);
                    continue;
                }

                var value = method.invoke(proxy);
                Object serialized;
                try {
                    serialized = this.registry.trySerialize(value).orElse(null);
                } catch (Throwable throwable) {
                    throw new IllegalStateException("Unable to serialize '%s'".formatted(cached.getRoute().join('.')), throwable);
                }

                this.document.set(cached.getRoute(), serialized == null ? value : serialized);

                var lines = getComments(method);

                this.document.getOptionalBlock(cached.getRoute()).ifPresent(block -> {
                    block.removeComments();
                    Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, lines);
                });
            }
        }

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
            if (annotation instanceof Comment comment) {
                if(comment.value().isEmpty()) {
                    comments.add(Optional.empty());
                    continue;
                }

                comments.add(Optional.of(comment.prefix() + comment.value()));
            }

            if (annotation instanceof me.ikevoodoo.spigotcore.config.annotations.comments.Comments commentsAnnotation) {
                for (var comment : commentsAnnotation.value()) {
                    if(comment.value().isEmpty()) {
                        comments.add(Optional.empty());
                        continue;
                    }

                    comments.add(Optional.of(comment.prefix() + comment.value()));
                }
            }
        }

        return comments.stream().map(optionalLine ->
                optionalLine.map(line -> new CommentLine(Optional.empty(), Optional.empty(), line, CommentType.BLOCK))
                        .orElse(Comments.BLANK_LINE)).toList();
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
