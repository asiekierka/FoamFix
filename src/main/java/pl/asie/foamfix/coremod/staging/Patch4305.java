package pl.asie.foamfix.coremod.staging;

public class Patch4305 {
    public static float diffuseLight(float x, float y, float z) {
        return x * x * 0.6f + y * y * ((3f + y) / 4f) + z * z * 0.8f;
    }
}
