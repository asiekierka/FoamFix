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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/**
 * Created by asie on 9/25/15.
 */
public class CommandGhostBuster extends CommandBase {
	@Override
	public String getName() {
		return "ghostbuster";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/ghostbuster [on|off]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length >= 1) {
			if ("on".equals(args[0])) {
				ChunkProviderServerWrapped.debugChunkProviding = true;
				sender.sendMessage(new TextComponentString("Ghost chunkload logging ON!"));
				return;
			} else if ("off".equals(args[0])) {
				ChunkProviderServerWrapped.debugChunkProviding = false;
				sender.sendMessage(new TextComponentString("Ghost chunkload logging OFF!"));
				return;
			}
		}

		sender.sendMessage(new TextComponentString("Ghost chunkload logging status: " + (ChunkProviderServerWrapped.debugChunkProviding ? "ON" : "OFF")));
		return;
	}
}
