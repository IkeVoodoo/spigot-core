package me.ikevoodoo.spigotcore.entities;

import me.ikevoodoo.spigotcore.entities.context.EntityClickContext;
import me.ikevoodoo.spigotcore.entities.context.EntitySetupContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
@SuppressWarnings("unused")
public abstract class Entity {

    private static final Random RANDOM = new SecureRandom();
    private static final NamespacedKey ID_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(Entity.class), "custom_entity_key");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(?<varname>[^}]+)}");
    private final String id;

    protected Entity() {
        this.id = EntityRegistry.getId(this.getClass());

        if (!this.hasState()) {
            EntityRegistry.registerStatelessInstance(this);
        }
    }

    public void onTick(@NotNull SpawnedEntity entity) {

    }

    /**
     * Called whenever the player right-clicks on this entity.
     *
     * @param context The click context will never be null.
     * @since 1.0.0
     *
     * @see EntityClickContext
     * */
    public void onRightClick(@NotNull EntityClickContext context) {
        // No-Op
    }

    /**
     * Called whenever the player left-clicks on this entity.
     *
     * @param context The click context will never be null.
     * @since 1.0.0
     *
     * @see EntityClickContext
     * */
    public void onLeftClick(@NotNull EntityClickContext context) {
        // No-Op
    }

    /**
     * Creates an entity and spawns it at a location.
     *
     * @param location The location to use, must not be null.
     * @since 1.0.0
     * */
    @Nullable
    public final SpawnedEntity spawn(@NotNull Location location) {
        Objects.requireNonNull(location, "Cannot spawn entity at null location!");
        Objects.requireNonNull(location.getWorld(), "Cannot spawn entity at null world!");

        var root = location.getWorld().spawnEntity(location, EntityType.MARKER);
        var variables = new EntityVariables(root.getPersistentDataContainer());
        var context = new EntitySetupContext(variables);

        this.setupEntity(context); // To set up variables and the likes

        var entities = new HashMap<String, SpawnedEntityPart>();
        for (var entry : context.getParts().entrySet()) {
            var part = entry.getValue();
            var spawned = part.spawn(location);

            var difference = spawned.getLocation().subtract(location.clone()).toVector();

            entities.put(entry.getKey(), new SpawnedEntityPart(spawned.getUniqueId(), difference));
        }

        var entity = new SpawnedEntity(this, root.getUniqueId(), entities, location);
        try {
            entity.save();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to save entity", e);
            return null;
        }

        return entity;
    }

    /**
     * @return the id associated with this item.
     * @since 1.0.0
     * */
    public final String getId() {
        return id;
    }

    /**
     * Gets the rate at which this entity should be ticked.
     *
     * @since 1.0.0
     * */
    public abstract int getTickRate();

    /**
     * Called near the beginning of {@link #spawn(Location)} to set up variables and the likes.
     *
     * @param context The setup context to use will never be null.
     * @since 1.0.0
     *
     * @see EntitySetupContext
     * @see #spawn(Location)
     * */
    protected abstract void setupEntity(EntitySetupContext context);

    /**
     * Does this entity change its data over time? The result of this method should never change.
     *
     * @since 1.0.0
     * */
    protected abstract boolean hasState();

}
