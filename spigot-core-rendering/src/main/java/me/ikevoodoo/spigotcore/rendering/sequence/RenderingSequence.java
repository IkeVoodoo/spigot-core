package me.ikevoodoo.spigotcore.rendering.sequence;

import me.ikevoodoo.spigotcore.rendering.ParticleRenderer;
import me.ikevoodoo.spigotcore.rendering.maths.EulerRotationMatrix;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class RenderingSequence {

    private final ParticleRenderer parent;
    private final Particle particle;
    private final Particle.DustOptions options;
    private final List<RenderingElement> elements = new ArrayList<>();

    private boolean finished;
    private double radiansX;
    private double radiansY;
    private double radiansZ;
    private EulerRotationMatrix rotationMatrix;

    public RenderingSequence(ParticleRenderer parent, Particle particle, Particle.DustOptions options) {
        this.parent = parent;
        this.particle = particle;
        this.options = options;
    }

    public RenderingSequence rotate(double radiansX, double radiansY, double radiansZ) {
        this.radiansX = radiansX;
        this.radiansY = radiansY;
        this.radiansZ = radiansZ;
        return this;
    }

    public RenderingSequence rotate(EulerRotationMatrix rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
        return this;
    }

    public RenderingSequence add(RenderingElement element) {
        if (element == null) return this;

        this.elements.add(element);
        return this;
    }

    public ParticleRenderer endSequence() {
        if (this.finished) return this.parent;
        this.finished = true;

        if (this.rotationMatrix != null) {
            this.rotationMatrix = new EulerRotationMatrix(this.radiansX, this.radiansY, this.radiansZ);
        }

        return this.parent;
    }



}
