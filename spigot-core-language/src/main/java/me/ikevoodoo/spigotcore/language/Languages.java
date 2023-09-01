package me.ikevoodoo.spigotcore.language;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public final class Languages {

    private final Map<String, LanguageFile> idToLanguage;
    private LanguageFile selected;

    public Languages(Map<String, LanguageFile> idToLanguage) {
        this.idToLanguage = idToLanguage;

        var english = idToLanguage.get("en_us");
        if (english != null) {
            this.selected = english;
            return;
        }

        if (this.idToLanguage.isEmpty()) {
            return;
        }

        this.selected = idToLanguage.values().iterator().next();
    }

    public static Languages fromFileArray(File... files) throws IOException {
        var map = new HashMap<String, LanguageFile>();

        for (var file : files) {
            var name = StringUtils.substringBeforeLast(file.getName(), ".").toLowerCase(Locale.ROOT);
            map.put(name, LanguageFile.loadFromFile(file));
        }

        return new Languages(map);
    }

    public static Languages loadFromResourcesFolder(Class<?> base, String path) throws URISyntaxException, IOException {
        var res = base.getResource(path);
        if (res == null) {
            throw new IllegalArgumentException("Cannot load languages from non-existent resource " + path);
        }

        var baseURI = res.toURI();
        try(
                var fs = FileSystems.getFileSystem(baseURI);
                var walk = Files.walk(fs.getPath(path), 1);
        ) {
            var map = new HashMap<String, LanguageFile>();

            walk.forEach(child -> {
                try {
                    var name = child.getFileName().toString();
                    if (!name.endsWith(".lang")) return;

                    name = StringUtils.substringBeforeLast(name, ".").toLowerCase(Locale.ROOT);

                    map.put(name, LanguageFile.loadFromLines(Files.readAllLines(child, StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });

            return new Languages(map);
        }
    }

    public void dumpToFolder(File folder, boolean replaceExisting) throws IOException {
        if (!folder.isDirectory() && folder.exists()) {
            throw new IllegalArgumentException("Input file must be a folder!");
        }

        Files.createDirectories(folder.toPath());

        for (var entry : this.idToLanguage.entrySet()) {
            var child = new File(folder, "%s.lang".formatted(entry.getKey()));
            if (child.exists() && !replaceExisting) continue;

            Files.writeString(child.toPath(), entry.getValue().toString(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
            );
        }
    }

    @Nullable
    public LanguageFile selectLanguage(@Nullable String language) {
        this.selected = this.idToLanguage.get(language);
        return this.selected;
    }

    @Nullable
    public LanguageFile getSelected() {
        return this.selected;
    }

    @Nullable
    public LanguageFile getLanguageFor(CommandSender sender) {
        var locale = sender instanceof Player player ? player.getLocale() : "en_us";

        var lang = this.idToLanguage.getOrDefault(locale, this.selected);
        if (lang == null) {
            lang = this.idToLanguage.values().iterator().next();
        }

        return lang;
    }

    @Nullable
    @Contract("null -> null; !null -> !null")
    public String getTranslation(String key) {
        if (this.selected == null) return key;

        return this.selected.getTranslation(key);
    }

    @Nullable
    @Contract("null -> null; !null -> !null;")
    public String getTranslationColored(String key) {
        if (this.selected == null) return key;

        return this.selected.getTranslationColored(key);
    }

    public List<String> getLanguages() {
        return new ArrayList<>(this.idToLanguage.keySet());
    }

}
