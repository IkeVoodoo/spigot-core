package me.ikevoodoo.spigotcore.items.context.click;

import me.ikevoodoo.spigotcore.items.ItemVariables;
import me.ikevoodoo.spigotcore.items.context.ClickContext;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author IkeVoodoo
 * @since 1.1.0
 * */
public class ClickBlockContext extends ClickContext {

    @NotNull
    private final Block clickedBlock;

    @NotNull
    private final BlockFace clickFace;

    public ClickBlockContext(@NotNull LivingEntity clicker, @NotNull ItemVariables variables, @NotNull ItemStack stack,
                             @Nullable EquipmentSlot hand, @NotNull Block clickedBlock, @NotNull BlockFace clickFace) {
        super(clicker, variables, stack, hand);
        this.clickedBlock = clickedBlock;
        this.clickFace = clickFace;
    }

    @NotNull
    public Block getClickedBlock() {
        return clickedBlock;
    }

    @NotNull
    public BlockFace getClickFace() {
        return clickFace;
    }
}
