package me.ikevoodoo.spigotcore.entities.part;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EntityItemPart implements EntityPart {

    private final ItemStack stack;
    private final Vector position;

    public EntityItemPart(ItemStack stack, Vector position) {
        this.stack = stack;
        this.position = position;
    }

    @Override
    public Entity spawn(Location origin) {
        var clone = origin.clone().add(this.position);
        return origin.getWorld().spawn(clone, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setAI(false);
            stand.setGravity(false);
            stand.getEquipment().setHelmet(this.stack);
            stand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);

            // Make the coordinates of the item in line with the actual position
            var boundingBox = stand.getBoundingBox();
            stand.teleport(clone.add(-(boundingBox.getWidthX() / 2), -boundingBox.getHeight(), -(boundingBox.getWidthZ() / 2)));
        });
    }
}
