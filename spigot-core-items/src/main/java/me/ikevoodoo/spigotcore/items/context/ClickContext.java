package me.ikevoodoo.spigotcore.items.context;

import me.ikevoodoo.spigotcore.items.ItemVariables;
import org.bukkit.entity.LivingEntity;
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

    public void consumeItem() {
        this.stack.setAmount(Math.max(this.stack.getAmount() - 1, 0));
    }
}
