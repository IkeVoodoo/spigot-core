package me.ikevoodoo.spigotcore.rendering.maths;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EulerRotationMatrix {

    private final double[] matrix;

    public EulerRotationMatrix(double x, double y, double z) {
        var rollSin = Math.sin(x);
        var rollCos = Math.cos(x);

        var pitchSin = Math.sin(y);
        var pitchCos = Math.cos(y);

        var yawSin = Math.sin(z);
        var yawCos = Math.cos(z);

        this.matrix = new double[] {
                pitchCos * yawCos, (rollSin * pitchSin * yawCos - rollCos * yawSin), (rollSin * yawSin + rollCos * pitchSin * yawCos),
                pitchCos * yawSin, (rollCos * yawCos + rollSin * pitchSin * yawSin), (rollCos * pitchSin * yawSin - rollSin * yawCos),
                -pitchSin, rollSin * pitchCos, rollCos * pitchCos
        };
    }

    /**
     * Rotates the given vector, mutating the parameter.
     *
     * @return The provided vector after rotation has been applied.
     * */
    @NotNull
    public double[] rotate(@NotNull double[] vector) {
        Objects.requireNonNull(vector, "Cannot rotate a null vector!");

        if (vector.length != 3) {
            throw new IllegalStateException("Vector length must be 3!");
        }

        var length = 3;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                vector[i] = this.matrix[i * length + j] * vector[j];
            }
        }

        return vector;
    }

}
