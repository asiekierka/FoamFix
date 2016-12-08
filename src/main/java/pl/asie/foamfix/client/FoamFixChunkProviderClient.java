package pl.asie.foamfix.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.common.FoamFixChunkSaveHandler;

import java.lang.reflect.Field;

public class FoamFixChunkProviderClient extends ChunkProviderClient {
	public static class Registrar {
		@SubscribeEvent
		public void onWorldLoadEvent(WorldEvent.Load event) {
			if (event.getWorld() instanceof WorldClient) {
				IChunkProvider provider = event.getWorld().getChunkProvider();
				if (provider.getClass() == ChunkProviderClient.class) {
					try {
						FoamFixChunkSaveHandler.CHUNK_PROVIDER_FIELD.set(event.getWorld(), new FoamFixChunkProviderClient(event.getWorld(), (ChunkProviderClient) provider));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					FoamFix.logger.warn("Could not override chunk provider on world " + event.getWorld().provider.getDimension() + " - class was " + provider.getClass().getName());
				}
			}
		}
	}

	private static final Field CHUNKPROVIDERCLIENT_CHUNKMAPPING = ReflectionHelper.findField(ChunkProviderClient.class, "chunkMapping", "field_73236_b");
	private final Long2ObjectMap<Chunk> chunkMapping;
	private long lastChunkKey = Long.MIN_VALUE;
	private Chunk lastChunk;

	public FoamFixChunkProviderClient(World w, ChunkProviderClient parent) {
		super(w);

		Long2ObjectMap<Chunk> map;
		try {
			map = (Long2ObjectMap<Chunk>) CHUNKPROVIDERCLIENT_CHUNKMAPPING.get(parent);
			CHUNKPROVIDERCLIENT_CHUNKMAPPING.set(this, map);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		chunkMapping = map;
	}

	@Override
	public void unloadChunk(int x, int z) {
		super.unloadChunk(x, z);
		long i = ChunkPos.asLong(x, z);
		if (lastChunkKey == i && lastChunk != null) {
			lastChunkKey = Long.MIN_VALUE;
			lastChunk = null;
		}
	}

	@Override
	public Chunk getLoadedChunk(int x, int z) {
		long i = ChunkPos.asLong(x, z);
		if (lastChunkKey == i && lastChunk != null) {
			return lastChunk;
		}
		lastChunkKey = i;
		lastChunk = chunkMapping.get(i);
		return lastChunk;
	}
}
