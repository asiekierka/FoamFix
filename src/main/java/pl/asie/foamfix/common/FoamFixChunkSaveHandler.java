package pl.asie.foamfix.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.foamfix.FoamFix;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

public class FoamFixChunkSaveHandler {
	public static final Field CHUNK_PROVIDER_FIELD = ReflectionHelper.findField(World.class, "chunkProvider", "field_73020_y");

	private long tick;
	private Multimap<Long, Chunk> chunksToSave = HashMultimap.create();
	private Map<Long, Pair<Long, Chunk>> unloadedChunkMap = new Long2ObjectOpenHashMap<>();

	private int unloadChunks(long tick, int maximum) {
		int unloaded = 0;
		Iterator<Chunk> iterator = chunksToSave.get(tick).iterator();
		while (iterator.hasNext()) {
			Chunk c = iterator.next();
			((FoamFixChunkProviderServer) c.getWorld().getChunkProvider()).saveChunk(c);
			unloadedChunkMap.remove(ChunkPos.asLong(c.xPosition, c.zPosition));
			iterator.remove();
			unloaded++;
			if (unloaded >= maximum) break;
		}
		return unloaded;
	}

	private int unloadChunks(long tick, World world, int maximum) {
		int unloaded = 0;
		Iterator<Chunk> iterator = chunksToSave.get(tick).iterator();
		while (iterator.hasNext()) {
			Chunk c = iterator.next();
			if (c.getWorld() == world) {
				((FoamFixChunkProviderServer) c.getWorld().getChunkProvider()).saveChunk(c);
				unloadedChunkMap.remove(ChunkPos.asLong(c.xPosition, c.zPosition));
				iterator.remove();
				unloaded++;
				if (unloaded >= maximum) break;
			}
		}
		return unloaded;
	}

	public void addChunk(Chunk chunk, int delay) {
		long i = ChunkPos.asLong(chunk.xPosition, chunk.zPosition);
		if (!unloadedChunkMap.containsKey(i)) {
			unloadedChunkMap.put(i, Pair.of(tick + delay, chunk));
			chunksToSave.put(tick + delay, chunk);
		}
	}

	public Chunk popUnloadingChunk(int x, int z) {
		long i = ChunkPos.asLong(x, z);
		Pair<Long, Chunk> c = unloadedChunkMap.get(i);
		if (c != null) {
			unloadedChunkMap.remove(i);
			chunksToSave.remove(c.getLeft(), c.getRight());
			return c.getRight();
		} else {
			return null;
		}
	}

	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		if (event.getWorld() instanceof WorldServer) {
			IChunkProvider provider = event.getWorld().getChunkProvider();
			if (provider.getClass() == ChunkProviderServer.class) {
				try {
					CHUNK_PROVIDER_FIELD.set(event.getWorld(), new FoamFixChunkProviderServer((ChunkProviderServer) provider));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				FoamFix.logger.warn("Could not override chunk provider on world " + event.getWorld().provider.getDimension() + " - class was " + provider.getClass().getName());
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		if (event.getWorld() instanceof WorldServer) {
			for (Long tick : unloadedChunkMap.keySet()) {
				unloadChunks(tick, event.getWorld(), Integer.MAX_VALUE);
			}
		}
	}

	@SubscribeEvent
	public void onServerTickEvent(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			unloadChunks(tick, 100);
			tick++;
		}
	}
}
