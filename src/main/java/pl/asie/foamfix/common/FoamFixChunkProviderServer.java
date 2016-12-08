/**
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
 */
package pl.asie.foamfix.common;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.ProxyCommon;
import pl.asie.foamfix.shared.FoamFixShared;

import javax.annotation.Nullable;
import java.io.IOException;

public class FoamFixChunkProviderServer extends ChunkProviderServer {
	private static final Logger LOGGER = LogManager.getLogger();

	private final TLongHashSet droppedChunksSet = new TLongHashSet();
	private final TLongHashSet loadingChunks = new TLongHashSet();
	private long lastChunkKey = Long.MIN_VALUE;
	private Chunk lastChunk;

	public FoamFixChunkProviderServer(ChunkProviderServer parent) {
		super(parent.worldObj, parent.chunkLoader, parent.chunkGenerator);
		id2ChunkMap.putAll(parent.id2ChunkMap);
	}

	@Override
	public void unload(Chunk chunkIn) {
		if (this.worldObj.provider.canDropChunk(chunkIn.xPosition, chunkIn.zPosition)) {
			this.droppedChunksSet.add(Long.valueOf(ChunkPos.asLong(chunkIn.xPosition, chunkIn.zPosition)));
			chunkIn.unloaded = true;
		}

		if (lastChunk == chunkIn) {
			lastChunkKey = Long.MIN_VALUE;
			lastChunk = null;
		}
	}

	@Override
	@Nullable
	public Chunk getLoadedChunk(int x, int z) {
		long i = ChunkPos.asLong(x, z);
		if (lastChunkKey == i && lastChunk != null)
			return lastChunk;

		Chunk chunk = this.id2ChunkMap.get(i);

		if (chunk != null) {
			chunk.unloaded = false;
			lastChunkKey = i;
			lastChunk = chunk;
		}

		return chunk;
	}

	@Override
	@Nullable
	public Chunk loadChunk(int x, int z, Runnable runnable) {
		Chunk chunk = this.getLoadedChunk(x, z);
		if (chunk == null) {
			chunk = ProxyCommon.chunkSaveHandler.popUnloadingChunk(x, z);
			if (chunk != null) {
//				System.out.println("Chunk unload is lies!");
				chunk.unloaded = false;
				chunk.onChunkLoad();
				lastChunkKey = ChunkPos.asLong(x, z);
				lastChunk = chunk;
			} else {
				long pos = ChunkPos.asLong(x, z);
				chunk = net.minecraftforge.common.ForgeChunkManager.fetchDormantChunk(pos, this.worldObj);
				if (chunk != null || !(this.chunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader)) {
					if (!loadingChunks.add(pos))
						net.minecraftforge.fml.common.FMLLog.bigWarning("There is an attempt to load a chunk (%d,%d) in dimension %d that is already being loaded. This will cause weird chunk breakages.", x, z, this.worldObj.provider.getDimension());
					if (chunk == null) chunk = this.loadChunkFromFile(x, z);

					if (chunk != null) {
						this.id2ChunkMap.put(ChunkPos.asLong(x, z), chunk);
						chunk.onChunkLoad();
						chunk.populateChunk(this, this.chunkGenerator);
					}

					loadingChunks.remove(pos);
				} else {
					net.minecraft.world.chunk.storage.AnvilChunkLoader loader = (net.minecraft.world.chunk.storage.AnvilChunkLoader) this.chunkLoader;
					if (runnable == null)
						chunk = net.minecraftforge.common.chunkio.ChunkIOExecutor.syncChunkLoad(this.worldObj, loader, this, x, z);
					else if (loader.chunkExists(this.worldObj, x, z)) {
						// We can only use the async queue for already generated chunks
						net.minecraftforge.common.chunkio.ChunkIOExecutor.queueChunkLoad(this.worldObj, loader, this, x, z, runnable);
						return null;
					}
				}
			}
		}

		// If we didn't load the chunk async and have a callback run it now
		if (runnable != null) runnable.run();
		return chunk;

	}

	@Override
	public boolean unloadQueuedChunks() {
		if (!this.worldObj.disableLevelSaving) {
			if (!this.droppedChunksSet.isEmpty()) {
				for (ChunkPos forced : this.worldObj.getPersistentChunks().keySet()) {
					this.droppedChunksSet.remove(ChunkPos.asLong(forced.chunkXPos, forced.chunkZPos));
				}

				TLongIterator iterator = this.droppedChunksSet.iterator();

				for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
					long chunkPos = iterator.next();
					Chunk chunk = this.id2ChunkMap.get(chunkPos);

					if (chunk != null && chunk.unloaded) {
						chunk.onChunkUnload();
						// FoamFix: Do not perform saving here!
//						System.out.println("Chunk unload request!");
						ProxyCommon.chunkSaveHandler.addChunk(chunk, 20 * 10);
						this.id2ChunkMap.remove(chunkPos);
						++i;
					}
				}

				if (FoamFixShared.config.gePermanentWorldsSet.contains(worldObj.provider.getDimension())) {
					if (id2ChunkMap.size() == 0 && net.minecraftforge.common.ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 && !this.worldObj.provider.getDimensionType().shouldLoadSpawn()) {
						net.minecraftforge.common.DimensionManager.unloadWorld(this.worldObj.provider.getDimension());
					}
				}
			}

			this.chunkLoader.chunkTick();
		}

		return false;
	}

	@Nullable
	private Chunk loadChunkFromFile(int x, int z)
	{
		try
		{
			Chunk chunk = this.chunkLoader.loadChunk(this.worldObj, x, z);

			if (chunk != null)
			{
				chunk.setLastSaveTime(this.worldObj.getTotalWorldTime());
				this.chunkGenerator.recreateStructures(chunk, x, z);
			}

			return chunk;
		}
		catch (Exception exception)
		{
			LOGGER.error((String)"Couldn\'t load chunk", (Throwable)exception);
			return null;
		}
	}

	public void saveChunk(Chunk chunk) {
//		System.out.println("Chunk unload for real!");
		chunk.setLastSaveTime(this.worldObj.getTotalWorldTime());

		try {
			this.chunkLoader.saveChunk(this.worldObj, chunk);
		} catch (IOException exception) {
			LOGGER.error("Couldn\'t save chunk", exception);
		} catch (MinecraftException minecraftexception) {
			LOGGER.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", minecraftexception);
		}

		try {
			this.chunkLoader.saveExtraChunkData(this.worldObj, chunk);
		} catch (IOException exception) {
			LOGGER.error("Couldn\'t save chunk", exception);
		}

		net.minecraftforge.common.ForgeChunkManager.putDormantChunk(ChunkPos.asLong(chunk.xPosition, chunk.zPosition), chunk);
	}
}