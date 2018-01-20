package pl.asie.foamfix.ghostbuster;

import java.lang.reflect.Field;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.world.WorldEvent;

import pl.asie.foamfix.FoamFix;

public class GhostBusterEventHandler {
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld() instanceof WorldServer) {
			FoamFix.logger.info("Overriding ChunkProviderServer in dimension " + event.getWorld().provider.getDimension() + "!");
			ChunkProviderServerWrapped wrapped = new ChunkProviderServerWrapped((WorldServer) event.getWorld());

			try {
				Field f = ReflectionHelper.findField(World.class, "chunkProvider", "field_73020_y");
				f.setAccessible(true);
				f.set(event.getWorld(), wrapped);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
