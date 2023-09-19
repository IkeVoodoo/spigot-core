package me.ikevoodoo.spigotcore.entities.ticking;

import me.ikevoodoo.spigotcore.entities.SpawnedEntity;
import me.ikevoodoo.spigotcore.ticking.Ticker;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

public final class EntityTicker {

    private static final Ticker<SpawnedEntity> TICKER = new Ticker<>(entity -> entity.getCustomEntity().onTick(entity));

    private EntityTicker() {
        // Find insult
    }

    public static void startTicking(Plugin plugin) {
        TICKER.scheduleTick(plugin);
    }

    @ApiStatus.Internal
    public static void addEntity(SpawnedEntity entity) {
        TICKER.addElement(entity.getCustomEntity().getTickRate(), entity);
    }

    @ApiStatus.Internal
    public static void removeEntity(SpawnedEntity entity) {
        TICKER.removeElement(entity.getCustomEntity().getTickRate(), entity);
    }

}
