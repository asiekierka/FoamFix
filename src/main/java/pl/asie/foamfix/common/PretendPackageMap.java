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
package pl.asie.foamfix.common;

import java.util.*;
import java.util.jar.Manifest;

public class PretendPackageMap implements Map<Package, Manifest> {
	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean containsKey(Object o) {
		return false;
	}

	@Override
	public boolean containsValue(Object o) {
		return false;
	}

	@Override
	public Manifest get(Object o) {
		return null;
	}

	@Override
	public Manifest put(Package aPackage, Manifest manifest) {
		return null;
	}

	@Override
	public Manifest remove(Object o) {
		return null;
	}

	@Override
	public void putAll(Map<? extends Package, ? extends Manifest> map) {

	}

	@Override
	public void clear() {

	}

	@Override
	public Set<Package> keySet() {
		return Collections.emptySet();
	}

	@Override
	public Collection<Manifest> values() {
		return Collections.emptySet();
	}

	@Override
	public Set<Entry<Package, Manifest>> entrySet() {
		return Collections.emptySet();
	}
}
