package me.ikevoodoo.spigotcore.items.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class ItemListeners {

    private ItemListeners() {
        throw new IllegalStateException("Did you really just attempt this? The constructor is private for a reason you dingus.");
    }

    public static void registerAllListeners(@NotNull Plugin plugin) {
        var pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new ItemUseListener(),  plugin);
    }

}
