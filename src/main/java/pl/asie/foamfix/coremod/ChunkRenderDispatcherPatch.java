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
		if (!chunkRenderer.isNeedsUpdateCustom()) {
			return updateChunkLater(chunkRenderer);
		}

		chunkRenderer.getLockCompileTask().lock();
		boolean flag;

		try {
			ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

			try {
				this.renderWorker.processTask(chunkcompiletaskgenerator);
			} catch (InterruptedException var7) { }

			flag = true;
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag;
	}
}
