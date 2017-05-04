package com.demshape.factorization.lossfunction;

/**
 * Defines a loss function.
 */
public interface LossFunctionInterface {

    /**
     * @param predicted Predicted value
     * @param measured Measured value
     * @return Loss
     */
    float g(float predicted, float measured);

    /**
     * @param predicted Predicted value
     * @param measured Measured value
     * @return derivative
     */
    float gDeriv(float predicted, float measured);

}
