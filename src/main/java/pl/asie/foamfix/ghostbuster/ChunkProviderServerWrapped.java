package pl.asie.foamfix.ghostbuster;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;

public class ChunkProviderServerWrapped extends ChunkProviderServer {
	public static boolean debugChunkProviding;

	public ChunkProviderServerWrapped(WorldServer worldServer) {
		super(worldServer, worldServer.getChunkProvider().chunkLoader, worldServer.getChunkProvider().chunkGenerator);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		if (debugChunkProviding) {
			Chunk chunk = this.getLoadedChunk(x, z);
			if (chunk != null) {
				return chunk;
			}

			if (!world.getPersistentChunks().containsKey(new ChunkPos(x, z))) {
				int i = 0;
				StackTraceElement[] stea = new Throwable().getStackTrace();

				if (stea.length > 1 && stea[1].toString().startsWith("net.minecraft.server.management.PlayerChunkMapEntry")) {
					i = -1;
				}

				if (i >= 0 && !FoamFixShared.config.gbWrapperCountNotifyBlock) {
					for (StackTraceElement ste : stea) {
						if (ste.toString().startsWith("net.minecraft.world.World.markAndNotifyBlock")) {
							i = -1;
							break;
						}
					}
				}

				if (i >= 0) {
					FoamFix.logger.info("Block in chunk [" + x + ", " + z + "] may be ghostloaded!");

					for (StackTraceElement ste : stea) {
						try {
							Class c = this.getClass().getClassLoader().loadClass(ste.getClassName());
							if (MinecraftServer.class.isAssignableFrom(c)) {
								break;
							}
							if ((i++) > 0) {
								FoamFix.logger.info("- " + ste.toString());
							}
						} catch (Exception e) {

						}
					}
				}
			}
		}

		return super.provideChunk(x, z);
	}
}
