package me.ikevoodoo.spigotcore.entities.context;

import me.ikevoodoo.spigotcore.entities.SpawnedEntity;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public class EntityClickContext {

    @NotNull
    private final LivingEntity clicker;

    @NotNull
    private final ItemStack stack;

    @Nullable
    private final EquipmentSlot hand;
    @NotNull
    private final SpawnedEntity clickedEntity;

    @Nullable
    private final Vector clickPosition;

    private boolean cancelled;

    public EntityClickContext(@NotNull LivingEntity clicker, @NotNull ItemStack stack, @Nullable EquipmentSlot hand, @NotNull SpawnedEntity clickedEntity, @Nullable Vector clickPosition) {
        this.clicker = clicker;
        this.stack = stack;
        this.hand = hand;
        this.clickedEntity = clickedEntity;
        this.clickPosition = clickPosition;
    }

    /**
     * @return The entity that clicked the item.
     * @since 1.0.0
     * */
    @NotNull
    public LivingEntity clicker() {
        return clicker;
    }

    /**
     * @return The item stack that was used.
     * @since 1.0.0
     * */
    @NotNull
    public ItemStack stack() {
        return stack;
    }

    /**
     * @return The hand that was used to click.
     * @since 1.1.0
     * */
    @Nullable
    public EquipmentSlot hand() {
        return hand;
    }

    @NotNull
    public SpawnedEntity clickedEntity() {
        return clickedEntity;
    }

    @Nullable
    public Vector clickPosition() {
        return clickPosition;
    }

    /**
     * Tries to reduce the item stack count by one if the clicker isn't in creative/spectator.
     *
     * @see #stack()
     * @see GameMode
     *
     * @since 1.0.0
     * */
    public void tryConsumeItem() {
        if (this.clicker instanceof Player player && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        this.stack.setAmount(Math.max(this.stack.getAmount() - 1, 0));
    }

    public void cancelInteraction() {
        this.cancelled = true;
    }

    public boolean isInteractionCancelled() {
        return this.cancelled;
    }
}
