/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.foamfix.ghostbuster;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.FoamFix;

import java.lang.reflect.Field;

public class GhostBusterEventHandler {
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld() instanceof WorldServer) {
			FoamFix.getLogger().info("Overriding ChunkProviderServer in dimension " + event.getWorld().provider.getDimension() + "!");
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
