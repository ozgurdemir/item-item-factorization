package com.demshape.factorization.lossfunction;

/**
 * Squared error loss function.
 */
public class SquaredError implements LossFunctionInterface {

    public float g(float predicted, float measured) {
        return (float) Math.pow((predicted - measured), 2);
    }

    public float gDeriv(float predicted, float measured) {
        return 2.0f * (predicted - measured);
    }

}
