package me.ikevoodoo.spigotcore.debugging;

import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
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

        sb.append("§6On §e").append(Bukkit.getName()).append(" §6version §b").append(Bukkit.getVersion()).append("§3-").append(Bukkit.getBukkitVersion()).append('\n');
        var plugins = Bukkit.getPluginManager().getPlugins();
        sb.append("§6Installed plugins (").append(plugins.length).append("):\n");
        for (var otherPlugin : plugins) {
            var desc = otherPlugin.getDescription();
            sb.append("§6- §e").append(desc.getName()).append(" §bv").append(desc.getVersion()).append(" §6(§aAPI version §3").append(desc.getAPIVersion()).append("§6)\n");
        }

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
        writer.append("\n\nError:\n");

        if (this.error != null) {
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
}
