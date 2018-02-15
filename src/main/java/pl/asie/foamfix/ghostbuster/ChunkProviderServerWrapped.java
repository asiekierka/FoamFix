/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
 *
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

package pl.asie.foamfix.ghostbuster;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;

public class ChunkProviderServerWrapped extends ChunkProviderServer {
	public static boolean debugChunkProviding;

	public ChunkProviderServerWrapped(WorldServer worldServer) {
		super(worldServer, worldServer.getChunkProvider().chunkLoader, worldServer.getChunkProvider().chunkGenerator);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		if (debugChunkProviding) {
			Chunk chunk = this.getLoadedChunk(x, z);
			if (chunk != null) {
				return chunk;
			}

			if (!world.getPersistentChunks().containsKey(new ChunkPos(x, z))) {
				int i = 0;
				StackTraceElement[] stea = new Throwable().getStackTrace();

				if (stea.length > 1 && stea[1].toString().startsWith("net.minecraft.server.management.PlayerChunkMapEntry")) {
					i = -1;
				}

				if (i >= 0 && !FoamFixShared.config.gbWrapperCountNotifyBlock) {
					for (StackTraceElement ste : stea) {
						if (ste.toString().startsWith("net.minecraft.world.World.markAndNotifyBlock")) {
							i = -1;
							break;
						}
					}
				}

				if (i >= 0) {
					FoamFix.logger.info("Block in chunk [" + x + ", " + z + "] may be ghostloaded!");

					for (StackTraceElement ste : stea) {
						try {
							Class c = this.getClass().getClassLoader().loadClass(ste.getClassName());
							if (MinecraftServer.class.isAssignableFrom(c)) {
								break;
							}
							if ((i++) > 0) {
								FoamFix.logger.info("- " + ste.toString());
							}
						} catch (Exception e) {

						}
					}
				}
			}
		}

		return super.provideChunk(x, z);
	}
}
