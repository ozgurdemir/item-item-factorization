package com.demshape.factorization.lossfunction;

/**
 * Smoothed hinge loss function. Group is either -1 or +1.
 */
public class SmoothHinge implements LossFunctionInterface {

    public float g(float predicted, float group) {
        final float z = group * predicted;
        if (z <= 0)
            return 0.5f - z;
        if ((0 < z) && (z < 1))
            return (float) (0.5 * Math.pow(1.0 - z, 2));
        return 0;
    }

    public float gDeriv(float predicted, float group) {
        final float z = group * predicted;
        if (z <= 0)
            return group * -1.0f;
        if ((0 < z) && (z < 1))
            return group * (z - 1.0f);
        return 0;
    }

}
