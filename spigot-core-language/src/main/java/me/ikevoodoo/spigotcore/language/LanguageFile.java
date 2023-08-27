package me.ikevoodoo.spigotcore.language;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class LanguageFile {

    private final String name;
    private final Map<String, String> translations;

    public LanguageFile(String name, Map<String, String> translations) {
        this.name = name;
        this.translations = translations;
    }

    public static LanguageFile loadFromFile(File file) throws IOException {
        try(var reader = new BufferedReader(new FileReader(file))) {
            String name = null;
            var translations = new HashMap<String, String>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.trim().startsWith("#")) {
                    continue;
                }

                if (name == null) {
                    name = line;
                    continue;
                }

                var splitIndex = line.indexOf('=');

                var key = line.substring(0, splitIndex);
                String translation = null;

                if (splitIndex < line.length() - 1) {
                    translation = line.substring(splitIndex + 1);
                }

                translations.put(key, translation);
            }

            return new LanguageFile(name, translations);
        }
    }

    public String name() {
        return name;
    }

    @Nullable
    public String getTranslation(@Nullable String key) {
        return this.translations.getOrDefault(key, key);
    }
}
