package dev.refinedtech.spigotcore.nbt.pdc;

import dev.refinedtech.spigotcore.nbt.NBTTagContainer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class NBTPersistentDataAdapterContext implements PersistentDataAdapterContext {

    private final NBTTagContainer tagContainer;

    public NBTPersistentDataAdapterContext(NBTTagContainer nbtTagContainer) {
        this.tagContainer = nbtTagContainer;
    }

    @NotNull
    @Override
    public PersistentDataContainer newPersistentDataContainer() {
        return new NBTPersistentDataContainer(this.tagContainer);
    }
}
