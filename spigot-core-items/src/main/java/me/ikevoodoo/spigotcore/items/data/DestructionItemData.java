package me.ikevoodoo.spigotcore.items.data;

import org.jetbrains.annotations.NotNull;

/**
 * Non-functional class.
 * */
public record DestructionItemData(
        boolean destroy,
        int destroyAfterTicks
) {

    public static DestructionItemData destroyAfter(int ticks) {
        return new DestructionItemData(true, ticks);
    }

    public static DestructionItemData neverDestroy() {
        return new DestructionItemData(false, 0);
    }

    public void apply(@NotNull ItemData itemData) {

    }

}
