package me.ikevoodoo.spigotcore.items.context;

import me.ikevoodoo.spigotcore.items.ItemVariables;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public class ClickContext {

    @NotNull
    private final LivingEntity clicker;

    @NotNull
    private final ItemVariables variables;

    @NotNull
    private final ItemStack stack;

    @Nullable
    private final EquipmentSlot hand;

    private boolean cancelled;

    public ClickContext(@NotNull LivingEntity clicker, @NotNull ItemVariables variables, @NotNull ItemStack stack, @Nullable EquipmentSlot hand) {
        this.clicker = clicker;
        this.variables = variables;
        this.stack = stack;
        this.hand = hand;
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
     * @return The variables on this item.
     * @since 1.0.0
     * */
    @NotNull
    public ItemVariables variables() {
        return variables;
    }

    /**
     * @return The item stack that was clicked on.
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
