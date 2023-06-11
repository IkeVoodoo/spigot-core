import me.ikevoodoo.spigotcore.items.Item;
import me.ikevoodoo.spigotcore.items.ItemRegistry;
import me.ikevoodoo.spigotcore.items.context.ClickContext;
import me.ikevoodoo.spigotcore.items.context.SetupContext;
import me.ikevoodoo.spigotcore.items.data.DisplayItemData;
import me.ikevoodoo.spigotcore.items.data.ItemData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FireballItem extends Item {

    public static void main(String[] args) {
        ItemRegistry.register("fireball", FireballItem.class, FireballItem::new);

        var fireball = new FireballItem();

        ItemStack stack = fireball.createItemStack(new ItemData.Builder()
                .displayData(
                        new DisplayItemData.Builder()
                                .displayName("§6Epic Fireball")
                                .lore(List.of("§7Right click to shoot a §6fireball§7!", "§r", "§7Power: ${power}")) // Use ${name} notation to get a variable from the item
                                .build()
                ).build()
        );

        Bukkit.getPlayer("IkeVoodoo").getInventory().addItem(stack);

        // You can give a player the custom item by just giving them the stack
        // Item stacks should ideally not be reused if they have a state,
        // instead createItemStack should be called again to generate a unique stack.
        // The FireballItem does not have a state, as it only reads its variables and doesn't change them, so it's safe to reuse.
    }

    @Override
    public void onRightClick(@NotNull ClickContext context) {
        var clicker = context.clicker();
        var world = clicker.getWorld();

        // Get the variable "power" from the ItemStack, if it is not present, use the default of 1F.
        var power = context.variables().getFloat("power", 1F);

        world.spawn(clicker.getEyeLocation(), Fireball.class, fireball -> {
            fireball.setYield(power);
            fireball.setDirection(clicker.getEyeLocation().getDirection());
        });

        context.tryConsumeItem();
    }

    @Override
    protected Material getMaterial() {
        return Material.FIRE_CHARGE;
    }

    @Override
    protected void setupItemStack(SetupContext context) {
        context.variables().setFloat("power", ThreadLocalRandom.current().nextFloat(0.5F, 3F));
    }

    @Override
    protected boolean hasState() {
        return false;
    }

}