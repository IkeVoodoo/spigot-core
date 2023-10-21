package me.ikevoodoo.spigotcore.rendering.text;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class TextRenderingSizeRegistry {

    public static final int AUTO_CHAR_DISTANCE = -1;

    private final Map<Character, Integer> sizes;
    private final int defaultSize;
    private final int charDistance;

    private TextRenderingSizeRegistry(Map<Character, Integer> sizes, int defaultSize, int charDistance) {
        this.sizes = sizes;
        this.defaultSize = defaultSize;
        this.charDistance = charDistance;
    }

    public static TextRenderingSizeRegistry fromFont(Font font, int charDistance) {
        var metrics = new Canvas().getFontMetrics(font);

        var monospacedWith = metrics.charWidth('m');
        if (metrics.charWidth('i') == monospacedWith) {
            // Monospaced
            System.out.println("MONOSPACED");
            charDistance = charDistance == -1 ? metrics.getMaxAdvance() - monospacedWith : charDistance;

            return new TextRenderingSizeRegistry(new HashMap<>(), monospacedWith, charDistance);
        }

        var map = new HashMap<Character, Integer>();
        for (char c = 0x0; c <= Character.MAX_VALUE; c++) {
            if (font.canDisplay(c)) {
                map.put(c, metrics.charWidth(c));
            }
        }

        return new TextRenderingSizeRegistry(map, 0, charDistance);
    }

    public int getSize(char input) {
        return this.sizes.getOrDefault(input, this.defaultSize);
    }

    public int getTotalLength(String input) {
        var distance = this.charDistance * (input.length() - 1);
        if (this.defaultSize != 0) {
            // This calculates the individual size of the characters plus the size of the spacing between.
            // This is done for performance reasons.
            return (this.defaultSize * input.length()) + distance;
        }

        return input.chars().map(c -> getSize((char) c)).sum() + distance;
    }


    @Override
    public String toString() {
        return "TextRenderingSizeRegistry[" +
                "defaultSize=" + defaultSize +
                ", charDistance=" + charDistance +
                ']';
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        var registry = TextRenderingSizeRegistry.fromFont(Font.createFont(Font.PLAIN, (File) null), 2);

        System.out.println(registry);

        System.out.println(registry.getTotalLength("AABBCC"));
    }

}
