package pl.asie.foamfix.ghostbuster;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import pl.asie.foamfix.FoamFix;

public class ChunkProviderServerWrapped extends ChunkProviderServer {
	public static boolean debugChunkProviding;

	public ChunkProviderServerWrapped(WorldServer worldServer) {
		super(worldServer, worldServer.getChunkProvider().chunkLoader, worldServer.getChunkProvider().chunkGenerator);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		Chunk chunk = this.getLoadedChunk(x, z);
		if (chunk != null) {
			return chunk;
		}

		if (debugChunkProviding) {
			if (!world.getPersistentChunks().containsKey(new ChunkPos(x, z))) {
				/* if (GBListener.lt >= 0) {
					String blockLocStr = "[" + GBListener.l0 + ", " + GBListener.l1 + ", " + GBListener.l2 + ", dim " + GBListener.lw.provider.dimensionId + "]";
					String funcName = Transformer.WGL_HOOKS[GBListener.lt * 2 + 1];
					FoamFix.logger.info("Block at " + blockLocStr + " is being ghostloaded from " + funcName + "!");
				} else { */
					FoamFix.logger.info("Block in chunk [" + x + ", " + z + "] may be ghostloaded!");
				// }
				int i = 0;
				for (StackTraceElement ste : new Throwable().getStackTrace()) {
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

		return super.provideChunk(x, z);
	}
}
