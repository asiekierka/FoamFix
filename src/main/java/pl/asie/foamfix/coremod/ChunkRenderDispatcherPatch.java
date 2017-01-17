package pl.asie.foamfix.coremod;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.client.renderer.chunk.RenderChunk;
import pl.asie.foamfix.shared.FoamFixShared;

/**
 * Created by asie on 1/17/17.
 */
public class ChunkRenderDispatcherPatch {
	private ChunkRenderWorker renderWorker;

	// shim
	public boolean updateChunkLater(RenderChunk chunkRenderer) {
		return true;
	}

	public boolean updateChunkNow(RenderChunk chunkRenderer) {
		// TODO: Make the latter a config option again if we use this method for sth else
//		if (FoamFixShared.config.delayChunkUpdates) {
			return updateChunkLater(chunkRenderer);
//		}
	}
}
