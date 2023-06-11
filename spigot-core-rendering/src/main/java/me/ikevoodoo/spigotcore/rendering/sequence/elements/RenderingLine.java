package me.ikevoodoo.spigotcore.rendering.sequence.elements;

import me.ikevoodoo.spigotcore.rendering.sequence.RenderingElement;
import org.bukkit.util.Vector;

public record RenderingLine(Vector start, Vector end) implements RenderingElement {

}
