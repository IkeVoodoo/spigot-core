package me.ikevoodoo.spigotcore.entities.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
@SuppressWarnings("unused")
public final class EntityListeners {

    private EntityListeners() {
        throw new IllegalStateException("Did you really just attempt this? The constructor is private for a reason you dingus.");
    }

    /**
     * Registers every listener under me.ikevoodoo.spigotcore.entities.listeners
     *
     * @param plugin The plugin to register the listeners on.
     *
     * @since 1.0.0
     *
     * @see EntityLoadListener
     * */
    public static void registerAllListeners(@NotNull Plugin plugin) {
        var pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new EntityLoadListener(), plugin);
    }

}
