package me.ikevoodoo.spigotcore.offlineplayers.attributes;

import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;

public final class AttributeDefaults {

    private static final Map<Attribute, Double> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(Attribute.GENERIC_MAX_HEALTH, 20D);
        DEFAULTS.put(Attribute.GENERIC_FOLLOW_RANGE, 32D);
        DEFAULTS.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0D);
        DEFAULTS.put(Attribute.GENERIC_MOVEMENT_SPEED, 0.7D);
        DEFAULTS.put(Attribute.GENERIC_ATTACK_DAMAGE, 2D);
        DEFAULTS.put(Attribute.GENERIC_ARMOR, 0D);
        DEFAULTS.put(Attribute.GENERIC_ARMOR_TOUGHNESS, 0D);
        DEFAULTS.put(Attribute.GENERIC_ATTACK_KNOCKBACK, 0D);
        DEFAULTS.put(Attribute.GENERIC_ATTACK_SPEED, 4D);
        DEFAULTS.put(Attribute.GENERIC_LUCK, 0D);
        DEFAULTS.put(Attribute.GENERIC_FLYING_SPEED, 0.4D);
        DEFAULTS.put(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS, 0D);
    }

    private AttributeDefaults() {

    }

    public static double getDefault(Attribute attribute) {
        return DEFAULTS.getOrDefault(attribute, 0D);
    }

}
