/*
 * Copyright (c) 2016 neptunepink
 * Copyright (c) 2018 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.foamfix.common;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

// Actually a client-side world leak detector.
public class WorldNuller {
	private final List<UnloadEntry> unload_queue_client = Lists.newArrayList();
//	private final List<UnloadEntry> unload_queue_server = Lists.newArrayList();

	private WorldNuller() {

	}

	public static void init() {
//		FoamFix.logger.info("Registered server-side world unload notifier!");
//		MinecraftForge.EVENT_BUS.register(new ServerNuller());
	}

	public static void initClient() {
		FoamFix.logger.info("Registered client-side world unload notifier!");
		MinecraftForge.EVENT_BUS.register(new ClientNuller());
	}

	@SubscribeEvent
	public void queueWorldNull(WorldEvent.Unload event) {
		if (event.getWorld().isRemote) {
			unload_queue_client.add(new UnloadEntry(event.getWorld()));
		} else {
//			unload_queue_server.add(new UnloadEntry(event.getWorld()));
		}
	}

	static class UnloadEntry {
		WeakReference<World> worldRef;
		long timeOfUnload;
		String name;

		UnloadEntry(World world) {
			this.worldRef = new WeakReference<>(world);
			this.timeOfUnload = System.currentTimeMillis();
			name = "<" + world.toString() + " " + world.provider.getDimension() + " " + world.getChunkProvider().makeString() + ">";
		}

		static void log(String msg) {
			FoamFix.logger.info(msg);
		}

		static boolean spam = true;

		boolean tick(long now) {
			long passedTime = now - timeOfUnload;
			int delay = FoamFixShared.config.gbWorldUnloadTime * 1000;
			if (passedTime < delay) return false;
			World world = worldRef.get();
			if (world != null && FoamFixShared.config.gbForgeGCNonUnloaded) {
				world = null;
				log("Unloaded world " + name + " was not garbage collected, forcing GC as requested...");
				System.gc();
				world = worldRef.get();
			}
			int secs = delay / 1000;
			if (world == null) {
				log("Unloaded world " + name + " was garbage collected.");
				return true;
			}
			if (world instanceof WorldServer) {
				if (DimensionManager.getWorld(world.provider.getDimension()) == world) {
					return true;
				}

				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				if (server != null && server.worlds != null) {
					for (WorldServer w : server.worlds) {
						if (world == w) {
							// Don't panic. Minecraft keeps instances of vanilla worlds around.
							// This is fine. This is absolutely fine.
							return true;
						}
					}
				}
			}
			log("Unloaded world " + name + " is still hanging around after " + secs + " seconds.");
			if (spam) {
				log("This may be due to a world leak, or there is little memory pressure.");
				log("Use the relevant FoamFix options to adjust the wait time.");
				spam = false;
			}
			return true;
		}
	}

	void tick(TickEvent event, boolean server) {
		if (event.phase != TickEvent.Phase.END) return;
		List<UnloadEntry> unload_queue = server ? Collections.emptyList() : unload_queue_client;
		if (unload_queue.isEmpty()) return;
		long now = System.currentTimeMillis();
		unload_queue.removeIf(e -> e.tick(now));
	}

	public static class ServerNuller extends WorldNuller {
		@SubscribeEvent
		public void serverTick(TickEvent.ServerTickEvent event) {
			tick(event, true);
		}
	}

	public static class ClientNuller extends WorldNuller {
		@SubscribeEvent
		public void clientTick(TickEvent.ClientTickEvent event) {
			tick(event, false);
		}
	}
}
