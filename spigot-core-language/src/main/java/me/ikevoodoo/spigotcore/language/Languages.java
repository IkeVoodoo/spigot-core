package me.ikevoodoo.spigotcore.language;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
    public LanguageFile getLanguageFor(Player player) {
        var locale = player.getLocale();

        var lang = this.idToLanguage.getOrDefault(locale, this.selected);
        if (lang == null) {
            lang = this.idToLanguage.values().iterator().next();
        }

        return lang;
    }

    @Nullable
    public String getTranslation(String key) {
        if (this.selected == null) return key;

        return this.selected.getTranslation(key);
    }

    @Nullable
    public String getTranslationColored(String key) {
        if (this.selected == null) return key;

        return this.selected.getTranslationColored(key);
    }

    public List<String> getLanguages() {
        return new ArrayList<>(this.idToLanguage.keySet());
    }

}
