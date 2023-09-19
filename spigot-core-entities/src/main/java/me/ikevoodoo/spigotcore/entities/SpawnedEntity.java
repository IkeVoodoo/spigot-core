package me.ikevoodoo.spigotcore.entities;

import me.ikevoodoo.spigotcore.entities.ticking.EntityTicker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class SpawnedEntity {

    /**
     * Used to store where other parts of the entity are and their UUIDs for restart/reload storage.
     * */
    private static final Plugin PROVIDING_PLUGIN = JavaPlugin.getProvidingPlugin(SpawnedEntity.class);
    private static final NamespacedKey ENTITIES_KEY = new NamespacedKey(PROVIDING_PLUGIN, "stored_entities");

    @NotNull
    private final Entity entity;
    private final UUID root;

    /**
     * A map of part-name to entity id
     * */
    private final Map<String, SpawnedEntityPart> entities;

    private Location location;
    private boolean removed;

    SpawnedEntity(@NotNull Entity entity, UUID root, Map<String, SpawnedEntityPart> entities, Location location) {
        this.entity = Objects.requireNonNull(entity, "Attempted to pass null entity as SpawnedEntity's custom entity!");
        this.root = root;
        this.entities = entities;

        for (var part : entities.values()) {
            SpawnedEntityHolder.setInstance(part.id(), this);
        }

        EntityTicker.addEntity(this);

        this.location = location;
    }

    @NotNull
    public Entity getCustomEntity() {
        return entity;
    }

    public EntityVariables variables() {
        return new EntityVariables(getRootEntity().getPersistentDataContainer());
    }

    public Optional<Vector> getPartPosition(String partName) {
        return Optional.ofNullable(this.entities.get(partName)).map(part -> part.relativeLocation().clone());
    }

    public void setPartPosition(String partName, @NotNull Vector position) {
        Objects.requireNonNull(position, "Cannot teleport part to null position!");

        Optional.ofNullable(this.entities.get(partName)).ifPresent(part -> {
            var entity = Bukkit.getEntity(part.id());
            if (entity == null) {
                Bukkit.getLogger().log(Level.SEVERE, "Part '{}' of entity got removed!", partName);
                return;
            }

            part.relativeLocation().copy(position);

            entity.teleport(this.getLocation().add(position));
        });
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public boolean addPassenger(@Nullable String partName, org.bukkit.entity.Entity entity) {
        var seat = getSeat(partName);

        if (seat == null) {
            return false;
        }

        seat.addPassenger(entity);
        return true;
    }

    public boolean removePassenger(@Nullable String partName, org.bukkit.entity.Entity entity) {
        var seat = getSeat(partName);

        if (seat == null) {
            return false;
        }

        seat.removePassenger(entity);
        return true;
    }

    public void remove() {
        this.removed = true;

        this.stopTicking();

        getRootEntity().remove();

        for (var part : this.entities.entrySet()) {
            var entity = Bukkit.getEntity(part.getValue().id());
            if (entity == null) continue;

            entity.remove();
        }
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public void moveForwards(double amount) {
        var direction = this.location.getDirection();

        this.teleport(this.location.add(direction.multiply(amount)));
    }

    public void teleport(@NotNull Location location) {
        Objects.requireNonNull(location, "Cannot teleport entity to null location!");
        Objects.requireNonNull(location.getWorld(), "Cannot teleport entity to null world!");

        getRootEntity().teleport(location);

        this.location = location;

        var cloned = this.getLocation();
        for (var part : this.entities.entrySet()) {
            var entity = Bukkit.getEntity(part.getValue().id());
            if (entity == null) {
                Bukkit.getLogger().log(Level.SEVERE, "Part '{}' of entity got removed!", part.getKey());
                continue;
            }

            cloned.add(part.getValue().relativeLocation());
            entity.teleport(cloned);
            cloned.subtract(part.getValue().relativeLocation());
        }
    }

    @ApiStatus.Internal
    public void stopTicking() {
        EntityTicker.removeEntity(this);
    }

    private org.bukkit.entity.Entity getRootEntity() {
        var rootEntity = Bukkit.getEntity(this.root);
        if (rootEntity == null) {
            throw new IllegalStateException("Root part of entity got removed!");
        }

        return rootEntity;
    }

    private org.bukkit.entity.Entity getSeat(String partName) {
        org.bukkit.entity.Entity seat;
        if (partName == null) {
            seat = getRootEntity();
        } else {
            var part = this.entities.get(partName);
            if (part == null) {
                return null;
            }

            seat = Bukkit.getEntity(part.id());
        }

        return seat;
    }

    void save() throws IOException {
        var rootEntity = getRootEntity();
        var pdc = rootEntity.getPersistentDataContainer();

        var baos = new ByteArrayOutputStream();
        var dos = new DataOutputStream(baos);

        var entityId = this.entity.getId().getBytes(StandardCharsets.UTF_8);
        dos.writeInt(entityId.length);
        dos.write(entityId);

        dos.writeDouble(this.location.getX());
        dos.writeDouble(this.location.getY());
        dos.writeDouble(this.location.getZ());

        dos.writeFloat(this.location.getYaw());
        dos.writeFloat(this.location.getPitch());

        dos.writeInt(this.entities.size());

        // This is storing every part on the root entity, now why am I doing this?
        // Because when the server shuts down or the plugin is reloaded, we need to be able to find every part again,
        // But not all parts need to necessarily be in loaded chunks, which can cause issues when we want to teleport or
        // interact with them.
        // This is simply storing the name of the part, the UUID of the entity, and its location, that way we can make sure
        // that the entity associated with the part gets loaded properly.
        // This seems inefficient; however, it is just 49 bytes per entity, or 49KB per 1,000 entities.
        for (var entry : this.entities.entrySet()) {
            var part = entry.getValue();
            var id = part.id();
            var entity = Bukkit.getEntity(id);
            if (entity == null) {
                Bukkit.getLogger().log(Level.SEVERE, "Part {} was excluded from saving as the entity does not exist!", part);
                continue;
            }

            var name = entry.getKey().getBytes(StandardCharsets.UTF_8);

            dos.writeInt(name.length);
            dos.write(name);
            dos.writeLong(id.getMostSignificantBits());
            dos.writeLong(id.getLeastSignificantBits());

            var loc = entity.getLocation();
            dos.writeDouble(loc.getX());
            dos.writeDouble(loc.getY());
            dos.writeDouble(loc.getZ());

            dos.writeFloat(loc.getYaw());
            dos.writeFloat(loc.getPitch());
        }

        pdc.set(ENTITIES_KEY, PersistentDataType.BYTE_ARRAY, baos.toByteArray());
    }

    /**
     * This is an internal method.
     * */
    @ApiStatus.Internal
    public static SpawnedEntity loadFromEntity(org.bukkit.entity.Entity entity) throws IOException {
        var pdc = entity.getPersistentDataContainer();
        if (!pdc.has(ENTITIES_KEY, PersistentDataType.BYTE_ARRAY)) return null;

        var array = pdc.get(ENTITIES_KEY, PersistentDataType.BYTE_ARRAY);
        assert array != null;

        Bukkit.getLogger().log(Level.WARNING, Arrays.toString(array));

        var bais = new ByteArrayInputStream(array);
        var dis = new DataInputStream(bais);

        var idBytes = new byte[dis.readInt()];
        if(dis.read(idBytes) == -1) {
            throw new IllegalStateException("A plugin has externally modified the entity's save, unable to load entity!");
        }
        var entityId = new String(idBytes, StandardCharsets.UTF_8);

        var origin = new Location(
                entity.getWorld(),
                dis.readDouble(),
                dis.readDouble(),
                dis.readDouble(),
                dis.readFloat(),
                dis.readFloat()
        );

        var size = dis.readInt();
        var map = new HashMap<String, SpawnedEntityPart>();
        for (int i = 0; i < size; i++) {
            var nameBytes = new byte[dis.readInt()];
            if(dis.read(nameBytes) == -1) {
                throw new IllegalStateException("A plugin has externally modified the entity's save, unable to load entity!");
            }

            var name = new String(nameBytes, StandardCharsets.UTF_8);

            var uuid = new UUID(dis.readLong(), dis.readLong());
            var loc = new Location(entity.getWorld(), dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readFloat(), dis.readFloat());

            loc.getChunk().getEntities(); // Load the chunk and it's entities

            var loaded = Bukkit.getEntity(uuid);
            if (loaded == null) {
                Bukkit.getLogger().log(Level.SEVERE, "Attempted to load entity {} but it got deleted since saving!", name);
                continue;
            }

            map.put(name, new SpawnedEntityPart(uuid, loc.subtract(origin).toVector()));
        }

        var custom = EntityRegistry.getInstance(entityId);
        if (custom == null) {
            throw new IllegalStateException("Unable to get instance of entity " + entityId);
        }

        var spawned = new SpawnedEntity(custom, entity.getUniqueId(), map, origin);
        if (map.size() != size) {
            spawned.save(); // In case some entities do not exist anymore, re-save to save on space!
        }

        return spawned;
    }

}
