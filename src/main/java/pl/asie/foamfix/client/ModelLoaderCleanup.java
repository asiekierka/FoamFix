package pl.asie.foamfix.client;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.Map;

public class ModelLoaderCleanup {
	public class Ticker {
		@SubscribeEvent
		public void onClientTick(TickEvent.ClientTickEvent event) {
			if (loader != null) {
				FoamFix.logger.info("Cleaning up ModelLoader...");
				try {
					((Map) LOADING_EXCEPTIONS_GETTER.invoke(loader)).clear();
				} catch (Throwable t) {
					t.printStackTrace();
				}

				loader = null;
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}
	}

	private static final MethodHandle LOADING_EXCEPTIONS_GETTER = MethodHandleHelper.findFieldGetter(ModelLoader.class, "loadingExceptions");
	private final Ticker ticker = new Ticker();
	private ModelLoader loader;

	public void tick() {
		ticker.onClientTick(null);
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		loader = event.getModelLoader();
		MinecraftForge.EVENT_BUS.register(ticker);
	}
}
