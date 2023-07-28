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
package pl.asie.foamfix.shared;

import java.util.function.BooleanSupplier;

public class FoamFixShared {
	public static final String MOD_NAME_IDPATCH = "JustEnoughIDs/NotEnoughIDs";
	public static final String MOD_NAME_SPONGE = "SpongeForge";
	public static final FoamFixConfig config = new FoamFixConfig();
	public static boolean isCoremod = false;
	private static Boolean idPatchModPresent;
	private static Boolean spongePresent;
	public static int neDeflaterCompression;

	public static boolean emitWarningIfPresent(String featureName, BooleanSupplier supplier, String modName, boolean disables) {
		if (supplier.getAsBoolean()) {
			if (disables) {
				System.err.println(featureName + " has been force-disabled - " + modName + " detected!");
			} else {
				System.err.println(featureName + " may not work correctly - " + modName + " detected!");
			}
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
