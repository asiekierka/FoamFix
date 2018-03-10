package pl.asie.foamfix.coremod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.foamfix.coremod.injections.VertexLighterSmoothAoCheap;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;

public class VertexLighterOverrideHandler {
	private static final MethodHandle SMOOTH_LIGHT_GETTER = MethodHandleHelper.findFieldGetter(ForgeBlockModelRenderer.class, "lighterSmooth");
	private static final MethodHandle SMOOTH_LIGHT_SETTER = MethodHandleHelper.findFieldSetter(ForgeBlockModelRenderer.class, "lighterSmooth");
	private boolean isUsingMinimumLighter;

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && FoamFixShared.isCoremod) {
			updateSmoothLighting();
		}
	}

	public void updateSmoothLighting() {
		BlockModelRenderer bmr = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		if (bmr instanceof ForgeBlockModelRenderer && Minecraft.getMinecraft().world == null) {
			boolean newUse = (Minecraft.getMinecraft().gameSettings.ambientOcclusion == 1);
			newUse &= FoamFixShared.config.clCheapMinimumLighter;

			if (isUsingMinimumLighter != newUse) {
				isUsingMinimumLighter = newUse;

				try {
					SMOOTH_LIGHT_SETTER.invoke(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer(),
							isUsingMinimumLighter ? ThreadLocal.withInitial(() -> new VertexLighterSmoothAoCheap(Minecraft.getMinecraft().getBlockColors()))
									: ThreadLocal.withInitial(() -> new VertexLighterSmoothAo(Minecraft.getMinecraft().getBlockColors())));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
