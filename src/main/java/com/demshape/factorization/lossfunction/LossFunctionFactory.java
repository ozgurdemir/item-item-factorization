package com.demshape.factorization.lossfunction;

/**
 * Factory to create a loss function based on a string value.
 */
public class LossFunctionFactory {

    public static LossFunctionInterface get(String name) throws IllegalArgumentException {
        switch (name.toLowerCase().trim()) {
            case "logistic":
                return new LogisticRegression();
            case "hinge":
                return new SmoothHinge();
            case "mse":
                return new SquaredError();
        }
        throw new IllegalArgumentException("Invalid loss function parameter: " + name);
    }
}
