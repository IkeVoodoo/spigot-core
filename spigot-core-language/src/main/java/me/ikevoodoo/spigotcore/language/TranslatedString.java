package me.ikevoodoo.spigotcore.language;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class TranslatedString implements CharSequence {

    private final String string;

    public TranslatedString(String string) {
        this.string = string;
    }

    /**
     * Replaces ${countName} with the integer amount.<br><br>
     *
     * Replaces the following plural indications:<br>
     * - ${plural_s} is transformed to "s" if count is not 1, otherwise ""<br>
     * - ${plural_es} is transformed to "es" if count is not 1, otherwise ""<br>
     * - ${plural_ies} is transformed to "ies" if count is not 1, otherwise "y"<br>
     * */
    public TranslatedString replaceCount(String countName, Number count) {
        var pluralPrefix = countName.equalsIgnoreCase("count") ? "" : countName + "-";

        var isFloating = count.longValue() != count.doubleValue();

        return new TranslatedString(
                this.string
                        .replace("${" + countName + "}", String.valueOf(isFloating ? count.doubleValue() : count.longValue()))
                        .replace("${" + pluralPrefix + "plural_s}", count.longValue() == 1 ? "" : "s")
                        .replace("${" + pluralPrefix + "plural_es}", count.longValue() == 1 ? "" : "es")
                        .replace("${" + pluralPrefix + "plural_ies}", count.longValue() == 1 ? "y" : "es")
        );
    }

    @NotNull
    @Override
    public String toString() {
        return string;
    }

    @Override
    public char charAt(int index) {
        return string.charAt(index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return string.subSequence(beginIndex, endIndex);
    }

    @Override
    public int length() {
        return string.length();
    }

    public int codePointAt(int index) {
        return string.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return string.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return string.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return string.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, @NotNull char[] dst, int dstBegin) {
        string.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Deprecated(since = "1.1")
    public void getBytes(int srcBegin, int srcEnd, @NotNull byte[] dst, int dstBegin) {
        string.getBytes(srcBegin, srcEnd, dst, dstBegin);
    }

    public byte[] getBytes(@NotNull String charsetName) throws UnsupportedEncodingException {
        return string.getBytes(charsetName);
    }

    public byte[] getBytes(@NotNull Charset charset) {
        return string.getBytes(charset);
    }

    public byte[] getBytes() {
        return string.getBytes();
    }

    public boolean contentEquals(@NotNull StringBuffer sb) {
        return string.contentEquals(sb);
    }

    public boolean contentEquals(@NotNull CharSequence cs) {
        return string.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return string.equalsIgnoreCase(anotherString);
    }

    public int compareTo(@NotNull String anotherString) {
        return string.compareTo(anotherString);
    }

    public int compareToIgnoreCase(@NotNull String str) {
        return string.compareToIgnoreCase(str);
    }

    public boolean regionMatches(int toffset, @NotNull String other, int ooffset, int len) {
        return string.regionMatches(toffset, other, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, @NotNull String other, int ooffset, int len) {
        return string.regionMatches(ignoreCase, toffset, other, ooffset, len);
    }

    public boolean startsWith(@NotNull String prefix, int toffset) {
        return string.startsWith(prefix, toffset);
    }

    public boolean startsWith(@NotNull String prefix) {
        return string.startsWith(prefix);
    }

    public boolean endsWith(@NotNull String suffix) {
        return string.endsWith(suffix);
    }

    public int indexOf(int ch) {
        return string.indexOf(ch);
    }

    public int indexOf(int ch, int fromIndex) {
        return string.indexOf(ch, fromIndex);
    }

    public int lastIndexOf(int ch) {
        return string.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return string.lastIndexOf(ch, fromIndex);
    }

    public int indexOf(@NotNull String str) {
        return string.indexOf(str);
    }

    public int indexOf(@NotNull String str, int fromIndex) {
        return string.indexOf(str, fromIndex);
    }

    public int lastIndexOf(@NotNull String str) {
        return string.lastIndexOf(str);
    }

    public int lastIndexOf(@NotNull String str, int fromIndex) {
        return string.lastIndexOf(str, fromIndex);
    }

    public String substring(int beginIndex) {
        return string.substring(beginIndex);
    }

    public String substring(int beginIndex, int endIndex) {
        return string.substring(beginIndex, endIndex);
    }

    public String concat(@NotNull String str) {
        return string.concat(str);
    }

    public String replace(char oldChar, char newChar) {
        return string.replace(oldChar, newChar);
    }

    public boolean matches(@NotNull String regex) {
        return string.matches(regex);
    }

    public boolean contains(@NotNull CharSequence s) {
        return string.contains(s);
    }

    public String replaceFirst(@NotNull String regex, @NotNull String replacement) {
        return string.replaceFirst(regex, replacement);
    }

    public String replaceAll(@NotNull String regex, @NotNull String replacement) {
        return string.replaceAll(regex, replacement);
    }

    public String replace(@NotNull CharSequence target, @NotNull CharSequence replacement) {
        return string.replace(target, replacement);
    }

    public String[] split(@NotNull String regex, int limit) {
        return string.split(regex, limit);
    }

    public String[] split(@NotNull String regex) {
        return string.split(regex);
    }

    public String toLowerCase(@NotNull Locale locale) {
        return string.toLowerCase(locale);
    }

    public String toLowerCase() {
        return string.toLowerCase();
    }

    public String toUpperCase(@NotNull Locale locale) {
        return string.toUpperCase(locale);
    }

    public String toUpperCase() {
        return string.toUpperCase();
    }

    public String trim() {
        return string.trim();
    }

    public String strip() {
        return string.strip();
    }

    public String stripLeading() {
        return string.stripLeading();
    }

    public String stripTrailing() {
        return string.stripTrailing();
    }

    public boolean isBlank() {
        return string.isBlank();
    }

    public Stream<String> lines() {
        return string.lines();
    }

    public String indent(int n) {
        return string.indent(n);
    }

    public String stripIndent() {
        return string.stripIndent();
    }

    public String translateEscapes() {
        return string.translateEscapes();
    }

    public <R> R transform(Function<? super String, ? extends R> f) {
        return string.transform(f);
    }

    public char[] toCharArray() {
        return string.toCharArray();
    }

    public String formatted(Object... args) {
        return string.formatted(args);
    }

    public String intern() {
        return string.intern();
    }

    public String repeat(int count) {
        return string.repeat(count);
    }

    public Optional<String> describeConstable() {
        return string.describeConstable();
    }

    public String resolveConstantDesc(MethodHandles.Lookup lookup) {
        return string.resolveConstantDesc(lookup);
    }
}
