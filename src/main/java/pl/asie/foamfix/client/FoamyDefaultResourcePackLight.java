package pl.asie.foamfix.client;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * More compatibility-friendly version of FoamyDefaultResourcePack, used
 * for example by OptiFine.
 */
public class FoamyDefaultResourcePackLight extends DefaultResourcePack {
	private final Set<ResourceLocation> foundResources = new HashSet<>();

	public FoamyDefaultResourcePackLight(ResourceIndex resourceIndexIn) {
		super(resourceIndexIn);
	}

	@Override
	public boolean resourceExists(ResourceLocation loc) {
		if (foundResources.contains(loc)) return true;

		if (super.resourceExists(loc)) {
			foundResources.add(loc);
			return true;
		}

		return false;
	}
}
