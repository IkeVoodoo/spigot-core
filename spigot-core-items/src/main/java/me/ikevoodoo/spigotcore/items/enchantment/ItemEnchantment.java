package me.ikevoodoo.spigotcore.items.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public enum ItemEnchantment {

    PROTECTION,
    FIRE_PROTECTION,
    FEATHER_FALLING,
    BLAST_PROTECTION,
    PROJECTILE_PROTECTION,
    RESPIRATION,
    AQUA_AFFINITY,
    THORNS,
    DEPTH_STRIDER,
    FROST_WALKER,
    CURSE_OF_BINDING("binding_curse"),
    SHARPNESS,
    SMITE,
    BANE_OF_ARTHROPODS,
    KNOCKBACK,
    FIRE_ASPECT,
    LOOTING,
    SWEEPING_EDGE("sweeping"),
    EFFICIENCY,
    SILK_TOUCH,
    UNBREAKING,
    FORTUNE,
    POWER,
    PUNCH,
    FLAME,
    INFINITY,
    LUCK_OF_THE_SEA,
    LURE,
    LOYALTY,
    IMPALING,
    RIPTIDE,
    CHANNELING,
    MULTISHOT,
    QUICK_CHARGE,
    PIERCING,
    MENDING,
    CURSE_OF_VANISHING("vanishing_curse"),
    SOUL_SPEED;

    private Enchantment bukkitEnchantment;

    ItemEnchantment() {
        this.setBukkitEnchantment(null);
    }

    ItemEnchantment(String key) {
        this.setBukkitEnchantment(key);
    }

    @NotNull
    public Enchantment toBukkit() {
        return this.bukkitEnchantment;
    }

    // Cannot use this.name() inside a constructor.
    private void setBukkitEnchantment(String key) {
        this.bukkitEnchantment = Enchantment.getByKey(NamespacedKey.minecraft(key == null ? this.name() : key));
    }

}
