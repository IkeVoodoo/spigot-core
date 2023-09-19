package me.ikevoodoo.spigotcore.language;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LanguageFile {

    private final String name;

    private final Map<String, String> comments;
    private final Map<String, String> translations;
    private final Map<String, TranslatedString> translationsStrings = new HashMap<>();

    public LanguageFile(String name, Map<String, String> comments, Map<String, String> translations) {
        this.name = name;
        this.comments = comments;
        this.translations = translations;
    }

    public static LanguageFile loadFromFile(File file) throws IOException {
        return loadFromLines(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
    }

    public static LanguageFile loadFromLines(List<String> lines) {
        var currentComment = new StringBuilder();
        var comments = new HashMap<String, String>();
        var translations = new LinkedHashMap<String, String>(); // Keeps order

        for (var line : lines) {
            if (line.isBlank() || line.trim().startsWith("#")) {
                currentComment.append(line).append('\n');
                continue;
            }

            var splitIndex = line.indexOf('=');


            var key = line.substring(0, splitIndex).trim();
            String translation = null;

            if (splitIndex < line.length() - 1) {
                translation = line.substring(splitIndex + 1).trim();
            }

            comments.put(key, currentComment.toString());
            currentComment.setLength(0);
            translations.put(key, translation);
        }

        return new LanguageFile(translations.get("name"), comments, translations);
    }

    public String name() {
        return name;
    }

    @Nullable
    @Contract("null -> null; !null -> !null")
    public TranslatedString getTranslation(@Nullable String key) {
        if (key == null) return null;

        return this.translationsStrings.computeIfAbsent(key, str -> new TranslatedString(this.translations.getOrDefault(key, key)));
    }

    @Nullable
    @Contract("null -> null; !null -> !null")
    public TranslatedString getTranslationColored(@Nullable String key) {
        var text = this.translations.getOrDefault(key, key);
        if (text == null) return null;

        var colored = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text)));

        return new TranslatedString(colored.toLegacyText()); // This from/to legacy text converts hex and the likes to the proper format.
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        for (var line : this.translations.entrySet()) {
            sb.append(this.comments.get(line.getKey()));
            sb.append(line.getKey()).append(" = ").append(line.getValue()).append('\n');
        }

        return sb.toString();
    }

    public TranslatedString toTranslatedString() {
        return new TranslatedString(this.toString());
    }
}
