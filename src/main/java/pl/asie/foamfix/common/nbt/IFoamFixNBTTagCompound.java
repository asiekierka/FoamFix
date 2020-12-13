package pl.asie.foamfix.common.nbt;

import net.minecraft.nbt.NBTBase;

import java.util.Map;

public interface IFoamFixNBTTagCompound {
	Map<String, NBTBase> foamfix_getTagMap();
}
