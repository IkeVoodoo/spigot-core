package me.ikevoodoo.spigotcore.debugging;

import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public final class DebugReport {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String text;
    private final Throwable error;

    public DebugReport(@NotNull String text, @Nullable Throwable error) {
        this.text = text;
        this.error = error;
    }

    public DebugReport(@NotNull String text) {
        this(text, null);
    }

    public static DebugReport debugPlugin(@NotNull Plugin plugin) {
        var description = plugin.getDescription();

        var sb = new StringBuilder();
        sb.append("§6==== §e")
                .append(description.getName()).append(" §bv").append(description.getVersion()).append(" §6(§aAPI version §3").append(description.getAPIVersion())
                .append("§6) ====\n");

        sb.append("§6On §e").append(Bukkit.getName()).append(" §6version§3 ").append(Bukkit.getBukkitVersion()).append(" §6commit §b").append(Bukkit.getVersion()).append('\n');
        var plugins = Bukkit.getPluginManager().getPlugins();
        var map = new HashMap<String, List<PluginDescriptionFile>>();

        for (var otherPlugin : plugins) {
            var desc = otherPlugin.getDescription();

            var list = map.computeIfAbsent(desc.getAPIVersion(), ver -> new ArrayList<>());
            list.add(desc);
        }

        for (var entry : map.entrySet()) {
            var ver = entry.getKey();
            var descriptions = entry.getValue();

            sb.append("§aAPI version §3").append(ver).append(" §a(").append(descriptions.size()).append("):\n");
            addDescriptions(sb, descriptions);
        }

        sb.append("§6Total plugin count is ").append(plugins.length);

        return new DebugReport(sb.toString());
    }

    public String getRawText() {
        return ChatColor.stripColor(this.getText());
    }

    public String getText() {
        return this.text;
    }

    public String getRawCompoundText() {
        return ChatColor.stripColor(this.getCompoundText());
    }

    public String getCompoundText() {
        var stringWriter = new StringWriter();
        var writer = new PrintWriter(stringWriter);
        writer.append(this.text);

        if (this.error != null) {
            writer.append("\n\nError:\n");
            this.error.printStackTrace(writer);
        }

        return stringWriter.toString();
    }

    public void sendTo(CommandSender sender) {
        sender.sendMessage(this.text);
    }

    @Nullable
    public String uploadToHastebin(@NotNull String baseUrl, @Nullable String token) {
        try {
            var url = new URL(baseUrl + "/documents");
            var connection = createConnection(token, url);

            if (connection.getResponseCode() != 200) {
                return null;
            }

            try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                var key = JSON_PARSER.parse(reader).getAsJsonObject().get("key");
                if (key == null) return null;

                return key.getAsString();
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public URL uploadToHastebinAndGetURL(@NotNull String baseUrl, @Nullable String token) throws MalformedURLException {
        var uploaded = this.uploadToHastebin(baseUrl, token);

        if (uploaded == null) {
            return null;
        }

        return new URL(baseUrl + "/" + uploaded);
    }

    @NotNull
    private HttpURLConnection createConnection(@Nullable String token, URL url) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Accept", "application/json");
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        connection.setDoOutput(true);
        connection.setDoInput(true);

        var output = connection.getOutputStream();
        var bytes = this.getRawCompoundText().getBytes(StandardCharsets.UTF_8);
        output.write(bytes);

        connection.connect();
        return connection;
    }

    private static void addDescriptions(StringBuilder sb, List<PluginDescriptionFile> descriptions) {
        var authors = new StringBuilder();

        for (var desc : descriptions) {
            sb.append("§6- §e").append(desc.getName()).append(" §bv").append(desc.getVersion());

            var authorList = splitAllAuthors(desc.getAuthors());
            if (authorList.isEmpty()) {
                sb.append('\n');
                continue;
            }

            sb.append(" §6by §e");

            if (authorList.size() == 1) {
                sb.append(authorList.get(0)).append('\n');
                continue;
            }

            for (int i = 0; i < authorList.size() - 1; i++) {
                authors.append(authorList.get(i)).append(", ");
            }

            authors.setLength(authors.length() - 2);
            authors.append(" and ").append(authorList.get(authorList.size() - 1));

            sb.append(authors).append("\n");

            authors.setLength(0);
        }
    }

    private static List<String> splitAllAuthors(List<String> authors) {
        var out = new ArrayList<String>();

        for (var author : authors) {
            Collections.addAll(out, author.split(", *"));
        }

        return out;
    }
}
