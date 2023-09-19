package me.ikevoodoo.spigotcore.entities.context;

import me.ikevoodoo.spigotcore.entities.EntityVariables;
import me.ikevoodoo.spigotcore.entities.part.EntityPart;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IkeVoodoo
 * @since 1.0.0
 * */
public class EntitySetupContext {

    private final EntityVariables variables;
    private final Map<String, EntityPart> parts = new HashMap<>();
    private final Map<String, EntityPart> partsView = Collections.unmodifiableMap(this.parts);

    public EntitySetupContext(EntityVariables variables) {
        this.variables = variables;
    }

    public EntityVariables variables() {
        return variables;
    }

    public void addPart(String key, EntityPart part) {
        this.parts.put(key, part);
    }

    public void removePart(String key) {
        this.parts.remove(key);
    }

    public boolean hasPart(String key) {
        return this.parts.containsKey(key);
    }

    public Map<String, EntityPart> getParts() {
        return this.partsView;
    }

}
