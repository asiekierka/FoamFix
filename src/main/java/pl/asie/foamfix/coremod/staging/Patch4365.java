package pl.asie.foamfix.coremod.staging;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

public class Patch4365 extends WorldProvider {
	public void getLightmapColors(float partialTicks, float sunBrightness, float skyLight, float blockLight, float[] colors) {
		getLightmapColors_foamfix_old(partialTicks, sunBrightness, skyLight, blockLight, colors);
		colors[0] = MathHelper.clamp(colors[0], 0f, 1f);
		colors[1] = MathHelper.clamp(colors[1], 0f, 1f);
		colors[2] = MathHelper.clamp(colors[2], 0f, 1f);
	}

	@Override
	public DimensionType getDimensionType() {
		return null;
	}

	public void getLightmapColors_foamfix_old(float partialTicks, float sunBrightness, float skyLight, float blockLight, float[] colors) {}
}
