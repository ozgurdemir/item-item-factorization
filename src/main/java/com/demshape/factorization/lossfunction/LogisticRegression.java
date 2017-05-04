package com.demshape.factorization.lossfunction;

/**
 * Logistic regression loss.
 * Group is either -1 or +1.
 */
public class LogisticRegression implements LossFunctionInterface {

    public float g(float predicted, float group) {
        return (float) Math.log(1.0 + Math.exp(-group * predicted));
    }

    public float gDeriv(float predicted, float group) {
        return (float) -(group / (Math.exp(group * predicted) + 1.0));
    }

}
