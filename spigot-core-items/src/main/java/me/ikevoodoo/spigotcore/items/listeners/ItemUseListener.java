package me.ikevoodoo.spigotcore.items.listeners;

import me.ikevoodoo.spigotcore.items.Item;
import me.ikevoodoo.spigotcore.items.ItemStackHolder;
import me.ikevoodoo.spigotcore.items.ItemVariables;
import me.ikevoodoo.spigotcore.items.context.ClickContext;
import me.ikevoodoo.spigotcore.items.context.click.ClickBlockContext;
import me.ikevoodoo.spigotcore.items.context.click.ClickEntityContext;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemUseListener implements Listener {

    protected ItemUseListener() {

    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        var stack = event.getItem();
        if (stack == null) return;

        var item = getItem(stack);
        if (item == null) return;

        var context =  getContext(event);
        if (context == null) return;

        var action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            item.onLeftClick(context);

            if (context.isInteractionCancelled()) {
                event.setCancelled(true);
            }
            return;
        }

        item.onRightClick(context);

        if (context.isInteractionCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();
        var stack = player.getInventory().getItem(event.getHand());

        var item = getItem(stack);
        if (item == null) return;

        var context = getContext(event);
        if (context == null) return;

        item.onRightClick(context);

        if (context.isInteractionCancelled()) {
            event.setCancelled(true);
        }
    }

    private ClickContext getContext(PlayerEvent event) {
        var player = event.getPlayer();

        EquipmentSlot hand = null;
        if (event instanceof PlayerInteractEvent interactEvent) {
            hand = interactEvent.getHand();
        } else if (event instanceof PlayerInteractEntityEvent interactEntityEvent) {
            hand = interactEntityEvent.getHand();
        }

        if (hand == null) {
            return null;
        }

        var stack = player.getInventory().getItem(hand);
        var meta = stack.getItemMeta();
        if (meta == null) return null;

        var item = getItem(stack);
        if (item == null) return null;

        var variables = new ItemVariables(meta.getPersistentDataContainer());
        return createContext(event, variables, stack);
    }

    private Item getItem(ItemStack stack) {
        var item = ItemStackHolder.getInstance(stack);
        if (item == null) {
            var id = Item.getItemStackId(stack);
            if (id == null) return null; // This item has literally no id,
            // How in a boundless hell full of tortured souls can we instantiate something with no id? Explain. Now.

            return ItemStackHolder.assignInstance(stack, id);
        }

        return item;
    }

    private ClickContext createContext(Event event, ItemVariables variables, ItemStack stack) {
        if (event instanceof PlayerInteractEntityEvent interactEntityEvent)  {
            return new ClickEntityContext(
                    interactEntityEvent.getPlayer(),
                    variables,
                    stack,
                    interactEntityEvent.getHand(),
                    interactEntityEvent.getRightClicked(),
                    interactEntityEvent instanceof PlayerInteractAtEntityEvent interactAtEntityEvent ? interactAtEntityEvent.getClickedPosition() : null
            );
        }

        if (event instanceof PlayerInteractEvent interactEvent) {
            var player = interactEvent.getPlayer();
            var action = interactEvent.getAction();
            var block = interactEvent.getClickedBlock();

            if (block != null && (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)) {
                return new ClickBlockContext(player, variables, stack, interactEvent.getHand(), block, interactEvent.getBlockFace());
            }

            return new ClickContext(player, variables, stack, interactEvent.getHand());
        }

        return null;
    }
}
