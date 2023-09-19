package me.ikevoodoo.spigotcore.entities;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnedEntityHolder {

    private static final Map<UUID, SpawnedEntity> ENTITY_TO_INSTANCE = new HashMap<>();

    private SpawnedEntityHolder() {
        // Find a good insult
    }

    @ApiStatus.Internal
    public static void setInstance(@NotNull UUID entity, SpawnedEntity item) {
        if (hasInstance(entity)) {
            throw new IllegalArgumentException("Cannot create an instance for an item stack twice!");
        }

        ENTITY_TO_INSTANCE.put(entity, item);
    }

    public static boolean hasInstance(@NotNull UUID entity) {
        return ENTITY_TO_INSTANCE.containsKey(entity);
    }

    @ApiStatus.Internal
    public static void deleteInstance(@NotNull UUID entity) {
        ENTITY_TO_INSTANCE.remove(entity);
    }


    @Nullable
    public static SpawnedEntity getInstance(@NotNull UUID entity) {
        try {
            return ENTITY_TO_INSTANCE.get(entity);
        } catch (ClassCastException ignored) {
            return null;
        }
    }

}
