package me.ikevoodoo.spigotcore.entities.listeners;

import me.ikevoodoo.spigotcore.entities.SpawnedEntity;
import me.ikevoodoo.spigotcore.entities.SpawnedEntityHolder;
import me.ikevoodoo.spigotcore.entities.ticking.EntityTicker;
import org.bukkit.entity.Marker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.io.IOException;

class EntityLoadListener implements Listener {

    protected EntityLoadListener() {

    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (var entity : event.getEntities()) {
            if (entity instanceof Marker) {
                System.out.println("Marker entity");
            }

            try {
                System.out.println("Testing entity: " + entity);

                var spawned = SpawnedEntity.loadFromEntity(entity);
                if (spawned == null) {
                    continue;
                }

                System.out.println("Found entity of type" + spawned.getCustomEntity().getId());

                SpawnedEntityHolder.setInstance(entity.getUniqueId(), spawned);

                EntityTicker.addEntity(spawned);
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }
}
