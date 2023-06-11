package me.ikevoodoo.spigotcore.rendering;

import me.ikevoodoo.spigotcore.rendering.maths.EulerRotationMatrix;
import me.ikevoodoo.spigotcore.rendering.sequence.RenderingSequence;
import me.ikevoodoo.spigotcore.rendering.sequence.elements.RenderingLine;
import me.ikevoodoo.spigotcore.rendering.sequence.elements.RenderingPoint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ParticleRenderer {

    private final World world;
    private final Vector origin;
    private final List<RenderingSequence> renderingSequences = new ArrayList<>();


    public ParticleRenderer(Location origin) {
        this.world = origin.getWorld();
        this.origin = origin.toVector();

        Objects.requireNonNull(this.world, "Origin world is null!");
    }

    public RenderingSequence nextSequence(Particle particle, Particle.DustOptions options) {
        this.finishCurrent();

        var sequence = new RenderingSequence(this, particle, options);
        this.renderingSequences.add(sequence);
        return sequence;
    }

    public RenderingSequence nextSequence(Particle particle) {
        return this.nextSequence(particle, null);
    }

    public void draw() {
        this.finishCurrent();

        // TODO
    }

    private void finishCurrent() {
        var current = this.renderingSequences.size() > 0 ? this.renderingSequences.get(this.renderingSequences.size() - 1) : null;
        if (current != null) {
            current.endSequence();
        }
    }

    public static void main(String[] args) {
        Location origin = null;

        EulerRotationMatrix rotationMatrix = new EulerRotationMatrix(10, 0, 0);

        ParticleRenderer renderer = new ParticleRenderer(origin);

        var left = new Vector(1, 0, 0);
        var top = new Vector(0, 2, 0);
        var right = new Vector(-1, 0, 0);

        var leftEdge = new RenderingLine(left, top);
        var rightEdge = new RenderingLine(top, right);
        var bottomEdge = new RenderingLine(left, right);

        renderer.nextSequence(Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(0, 250, 0), 1))
                .rotate(rotationMatrix) // radians
                .add(new RenderingPoint(0, 1, 0))
                .add(leftEdge)
                .add(rightEdge)
                .add(bottomEdge)
                .endSequence();

        renderer.draw();
    }


}
