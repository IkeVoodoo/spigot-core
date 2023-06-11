package me.ikevoodoo.spigotcore.items.context;

import me.ikevoodoo.spigotcore.items.ItemVariables;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClickContext {

    private final LivingEntity clicker;
    private final ItemVariables variables;
    private final ItemStack stack;

    public ClickContext(LivingEntity clicker, ItemVariables variables, ItemStack stack) {
        this.clicker = clicker;
        this.variables = variables;
        this.stack = stack;
    }

    @NotNull
    public LivingEntity clicker() {
        return clicker;
    }

    @NotNull
    public ItemVariables variables() {
        return variables;
    }

    /**
     * Tries to reduce the item stack count by one if the clicker isn't in creative/spectator.
     * */
    public void tryConsumeItem() {
        if (this.clicker instanceof Player player && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        this.stack.setAmount(Math.max(this.stack.getAmount() - 1, 0));
    }
}
