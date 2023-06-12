package me.ikevoodoo.spigotcore.items.listeners;

import me.ikevoodoo.spigotcore.items.Item;
import me.ikevoodoo.spigotcore.items.ItemStackHolder;
import me.ikevoodoo.spigotcore.items.ItemVariables;
import me.ikevoodoo.spigotcore.items.context.ClickContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemUseListener implements Listener {

    protected ItemUseListener() {

    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var stack = event.getItem();
        if (stack == null) return;

        var meta = stack.getItemMeta();
        if (meta == null) return;

        var item = ItemStackHolder.getInstance(stack);
        if (item == null) {
            var id = Item.getItemStackId(stack);
            if (id == null) return; // This item has literally no id,
            // How in a boundless hell full of tortured souls can we instantiate something with no id? Explain. Now.

            item = ItemStackHolder.assignInstance(stack, id);
        }

        var variables = new ItemVariables(meta.getPersistentDataContainer());
        var context = new ClickContext(player, variables, stack);

        var action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {

            item.onLeftClick(context);
            return;
        }

        item.onRightClick(context);
    }
}
