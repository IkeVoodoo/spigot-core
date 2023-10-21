package me.ikevoodoo.spigotcore.offlineplayers.attributes;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class OfflineAttributeInstance implements AttributeInstance {

    private final List<AttributeModifier> modifiers = new ArrayList<>();
    private final List<AttributeModifier> modifiersView = Collections.unmodifiableList(this.modifiers);
    private final double defaultValue;
    private final Attribute attribute;
    private double baseValue;

    public OfflineAttributeInstance(double defaultValue, Attribute attribute, double baseValue) {
        this.defaultValue = defaultValue;
        this.attribute = attribute;
        this.baseValue = baseValue;
    }

    @NotNull
    @Override
    public Attribute getAttribute() {
        return this.attribute;
    }

    @Override
    public double getBaseValue() {
        return this.baseValue;
    }

    @Override
    public void setBaseValue(double value) {
        this.baseValue = value;
    }

    @NotNull
    @Override
    public Collection<AttributeModifier> getModifiers() {
        return this.modifiersView;
    }

    @Override
    public void addModifier(@NotNull AttributeModifier modifier) {
        this.modifiers.add(modifier);
    }

    @Override
    public void removeModifier(@NotNull AttributeModifier modifier) {
        this.modifiers.remove(modifier);
    }

    @Override
    public double getValue() {
        var x = this.baseValue;

        for (var modifier : this.modifiers) {
            if (modifier.getOperation() != AttributeModifier.Operation.ADD_NUMBER) continue;

            x += modifier.getAmount();
        }

        var y = x;

        for (var modifier : this.modifiers) {
            if (modifier.getOperation() != AttributeModifier.Operation.ADD_SCALAR) continue;
            y += x * modifier.getAmount();
        }

        for (var modifier : this.modifiers) {
            if (modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_SCALAR_1) continue;

            y *= 1.0 + modifier.getAmount();
        }

        return y;
    }

    @Override
    public double getDefaultValue() {
        return this.defaultValue;
    }
}
