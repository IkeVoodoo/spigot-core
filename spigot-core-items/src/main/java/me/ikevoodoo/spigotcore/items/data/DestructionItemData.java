package me.ikevoodoo.spigotcore.items.data;

import org.jetbrains.annotations.NotNull;

/**
 * Non-functional class.
 * */
public record DestructionItemData(
        boolean destroy,
        int destroyAfterTicks
) {

    @NotNull
    public static DestructionItemData destroyAfter(int ticks) {
        return new DestructionItemData(true, ticks);
    }

    @NotNull
    public static DestructionItemData neverDestroy() {
        return new DestructionItemData(false, 0);
    }

    public void apply(@NotNull ItemData itemData) {

    }

}
