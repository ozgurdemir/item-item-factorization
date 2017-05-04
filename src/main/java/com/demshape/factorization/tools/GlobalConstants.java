package com.demshape.factorization.tools;

public class GlobalConstants {

    public static final float HASH_MAP_LOAD_FACTOR = 0.75f;

    public static int hashSize(int size) {
        return (int) (size / HASH_MAP_LOAD_FACTOR);
    }
}
