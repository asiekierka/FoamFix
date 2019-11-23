/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
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

/**
 * This file is part of FoamFixAPI.
 *
 * FoamFixAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFixAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFixAPI.  If not, see <http://www.gnu.org/licenses/>.
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
package pl.asie.foamfix.shared;

import java.util.function.BooleanSupplier;

public class FoamFixShared {
	public static final String MOD_NAME_IDPATCH = "JustEnoughIDs/NotEnoughIDs";
	public static final String MOD_NAME_SPONGE = "SpongeForge";
	public static final FoamFixConfig config = new FoamFixConfig();
	public static boolean isCoremod = false;
	public static int ramSaved = 0;
	private static Boolean idPatchModPresent;
	private static Boolean spongePresent;

	public static boolean emitWarningIfPresent(String featureName, BooleanSupplier supplier, String modName) {
		if (supplier.getAsBoolean()) {
			System.err.println(featureName + " has been force-disabled - " + modName + " detected!");
			return true;
		} else {
			return false;
		}
	}

	public static boolean hasIdPatch() {
		if (idPatchModPresent == null) {
			try {
				idPatchModPresent = Class.forName("org.dimdev.jeid.JEIDLoadingPlugin") != null;
			} catch (ClassNotFoundException e) {
				try {
					idPatchModPresent = Class.forName("ru.fewizz.neid.Neid") != null;
				} catch (ClassNotFoundException ee) {
					idPatchModPresent = false;
				}
			}
		}

		return false;
	}

	public static boolean hasSponge() {
		if (spongePresent == null) {
			try {
				spongePresent = Class.forName("org.spongepowered.mod.SpongeCoremod") != null;
			} catch (ClassNotFoundException e) {
				spongePresent = false;
			}
		}

		return spongePresent;
	}

	public static boolean hasOptifine() {
		try {
			return Class.forName("optifine.OptiFineTweaker") != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
