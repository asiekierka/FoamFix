package pl.asie.foamfix.coremod.blockinfo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.WeakHashMap;

public class ChunkInfoProvider {
	private static final WeakHashMap<IBlockAccess, ChunkInfo> infoMap = new WeakHashMap<>();

	@Nullable
	public static ChunkInfo getChunkInfo(IBlockAccess access, BlockPos pos) {
		if (access instanceof ChunkCache) {
			ChunkInfo info = infoMap.get(access);
			if (info == null) {
				info = new ChunkInfo(new BlockPos(pos.getX() & (~15), pos.getY() & (~15), pos.getZ() & (~15)));
				infoMap.put(access, info);
			}
			return info;
		} else {
			return null;
		}
	}
}
