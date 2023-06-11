package me.ikevoodoo.spigotcore.interactions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class InteractionChecker {

    private InteractionChecker() {

    }

    public static boolean isBlockInteractable(@NotNull HumanEntity entity,
                                              @NotNull EquipmentSlot hand,
                                              @NotNull Block block) {
        Objects.requireNonNull(entity, "Cannot check for interaction on a null entity!");
        Objects.requireNonNull(hand, "Cannot check for interaction on a null hand!");

        var inventory = entity.getInventory();

        var item = inventory.getItem(hand);

        if (canItemWaterLog(item, block)) {
            return true;
        }

        return isBlockInteractable(block);
    }

    public static boolean isBlockInteractable(@NotNull Block block) {
        Objects.requireNonNull(block, "Cannot check for interaction on a null block!");

        var type = block.getType();
        if (!type.isInteractable()) return false;

        // TODO maybe check if a block has no actual interaction usage?
        return true;
    }

    public static boolean canItemWaterLog(ItemStack stack, Block block) {
        var itemType = stack.getType();
        var blockType = block.getType();

        if (isBucketFull(stack) && blockType == Material.CAULDRON) {
            return true;
        }

        if (itemType == Material.BUCKET && isCauldronFull(block)) {
            return true;
        }

        if (!(block instanceof Waterlogged waterlogged)) {
            return false; // If the block can't be waterlogged, return
        }

        if (itemType == Material.WATER_BUCKET && !waterlogged.isWaterlogged()) {
            return true;
        }

        return itemType == Material.BUCKET && waterlogged.isWaterlogged();
    }

    public static boolean isBucketFull(@NotNull ItemStack stack) {
        Objects.requireNonNull(stack, "Cannot check if bucket is full on a null item!");

        return switch (stack.getType()) {
            case AXOLOTL_BUCKET, COD_BUCKET, LAVA_BUCKET, MILK_BUCKET, PUFFERFISH_BUCKET, SALMON_BUCKET, POWDER_SNOW_BUCKET, TROPICAL_FISH_BUCKET, WATER_BUCKET -> true;
            default -> false;
        };
    }

    public static boolean isCauldronFull(@NotNull Block block) {
        Objects.requireNonNull(block, "Cannot check if cauldron is full on an null block!");

        return switch (block.getType()) {
            case LAVA_CAULDRON, WATER_CAULDRON, POWDER_SNOW_CAULDRON -> true;
            default -> false;
        };
    }

}
