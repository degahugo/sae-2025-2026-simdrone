package fr.app.model;

public class UnderwaterUtils {

    public static float computeIntensity(float depth) {
        return Math.max(
                0.01f,
                1.0f - depth * 0.03f
        );
    }
}



