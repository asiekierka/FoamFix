package pl.asie.foamfix.coremod.staging;

import net.minecraft.util.EnumFacing;
import pl.asie.foamfix.util.FoamUtils;

import javax.vecmath.Vector3f;

public class Patch4313 {
    public static void fillNormal(int[] faceData, EnumFacing facing) {
        Vector3f v1 = new Vector3f(
                Float.intBitsToFloat(faceData[3 * 7 + 0]) - Float.intBitsToFloat(faceData[1 * 7 + 0]),
                Float.intBitsToFloat(faceData[3 * 7 + 1]) - Float.intBitsToFloat(faceData[1 * 7 + 1]),
                Float.intBitsToFloat(faceData[3 * 7 + 2]) - Float.intBitsToFloat(faceData[1 * 7 + 2])
        );
        Vector3f v2 = new Vector3f(
                Float.intBitsToFloat(faceData[2 * 7 + 0]) - Float.intBitsToFloat(faceData[0 * 7 + 0]),
                Float.intBitsToFloat(faceData[2 * 7 + 1]) - Float.intBitsToFloat(faceData[0 * 7 + 1]),
                Float.intBitsToFloat(faceData[2 * 7 + 2]) - Float.intBitsToFloat(faceData[0 * 7 + 2])
        );
        v1.cross(v2, v1);
        v1.normalize();

        int x = ((byte) Math.round(v1.x * 127)) & 0xFF;
        int y = ((byte) Math.round(v1.y * 127)) & 0xFF;
        int z = ((byte) Math.round(v1.z * 127)) & 0xFF;
        int w = x | (y << 0x08) | (z << 0x10);
        for (int i = 0; i < 4; i++) {
            faceData[i * 7 + 6] = w;
        }
    }
}
