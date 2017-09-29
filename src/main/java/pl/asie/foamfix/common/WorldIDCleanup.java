package pl.asie.foamfix.common;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.File;
import java.io.FileInputStream;

public class WorldIDCleanup {
	public static class TinyRegistry {
		private final IForgeRegistry parent;
		private final TObjectIntMap<ResourceLocation> map = new TObjectIntHashMap<>();
		private final TIntHashSet idsInUse = new TIntHashSet();
		private final String name;

		public TinyRegistry(IForgeRegistry parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		private void add(NBTTagCompound compound) {
			NBTTagList list = compound.getTagList("ids", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound c = list.getCompoundTagAt(i);
				if (c.hasKey("K") && c.hasKey("V")) {
					add(new ResourceLocation(c.getString("K")), c.getInteger("V"));
				}
			}
		}

		private void add(ResourceLocation location, int id) {
			map.put(location, id);
			if (parent.containsKey(location)) {
				idsInUse.add(id);
			}
		}

		public boolean shouldRemove(int id) {
			return !idsInUse.contains(id);
		}
	}

	private final TinyRegistry BLOCKS = new TinyRegistry(ForgeRegistries.BLOCKS, "minecraft:blocks");

	public WorldIDCleanup(File worldDirectory) throws Exception {
		File levelFile = new File(worldDirectory, "level.dat");
		if (!levelFile.exists()) {
			throw new Exception("Invalid world directory!");
		}

		NBTTagCompound levelData = CompressedStreamTools.readCompressed(new FileInputStream(levelFile));
		if (!levelData.hasKey("FML")) {
			throw new Exception("Non-Forge level.dat!");
		}

		NBTTagCompound levelData1 = levelData.getCompoundTag("FML");
		if (!levelData1.hasKey("Registries")) {
			throw new Exception("Corrupt level.dat!");
		}

		NBTTagCompound registriesCompound = levelData1.getCompoundTag("Registries");
		if (registriesCompound.hasKey(BLOCKS.name)) {
			BLOCKS.add(registriesCompound.getCompoundTag(BLOCKS.name));
		}
	}

	public void cleanup() {

	}
}
