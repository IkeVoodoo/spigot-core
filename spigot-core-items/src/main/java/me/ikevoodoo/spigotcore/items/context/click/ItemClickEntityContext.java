package me.ikevoodoo.spigotcore.items.context.click;

import me.ikevoodoo.spigotcore.items.ItemVariables;
import me.ikevoodoo.spigotcore.items.context.ItemClickContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author IkeVoodoo
 * @since 1.1.0
 * */
public class ItemClickEntityContext extends ItemClickContext {

    @NotNull
    private final Entity clickedEntity;

    @Nullable
    private final Vector clickPosition;

    public ItemClickEntityContext(@NotNull LivingEntity clicker, @NotNull ItemVariables variables, @NotNull ItemStack stack,
                                  @Nullable EquipmentSlot hand, @NotNull Entity clickedEntity, @Nullable Vector clickPosition) {
        super(clicker, variables, stack, hand);
        this.clickedEntity = clickedEntity;
        this.clickPosition = clickPosition;
    }

    @NotNull
    public Entity getClickedEntity() {
        return clickedEntity;
    }

    @Nullable
    public Vector getClickPosition() {
        return clickPosition;
    }
}
