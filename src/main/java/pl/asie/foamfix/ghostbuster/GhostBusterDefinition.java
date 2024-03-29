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

public class GhostBusterDefinition {
	public final String obfMethodName;
	public final String deobfMethodName;
	public final int accessPos;
	public final int posPos;
	public final int radius;
	public final Object returnValue;

	public GhostBusterDefinition(String obfMethodName, String deobfMethodName, int accessPos, int posPos, int radius) {
		this(obfMethodName, deobfMethodName, accessPos, posPos, radius, null);
	}

	public GhostBusterDefinition(String obfMethodName, String deobfMethodName, int accessPos, int posPos, int radius, Object returnValue) {
		this.obfMethodName = obfMethodName;
		this.deobfMethodName = deobfMethodName;
		this.accessPos = accessPos;
		this.posPos = posPos;
		this.radius = radius;
		this.returnValue = returnValue;
	}

	public static GhostBusterDefinition updateTick(int radius) {
		return new GhostBusterDefinition("func_180650_b", "updateTick", 1, 2, radius);
	}

	public static GhostBusterDefinition neighborChanged() {
		return new GhostBusterDefinition("func_189540_a", "neighborChanged", 2, 3, 1);
	}
}
