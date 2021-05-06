/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
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

package pl.asie.foamfix.client.deduplicator;

import net.minecraftforge.client.model.PerspectiveMapWrapper;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;

public class RegularImmutableListDeduplicatorFunction implements DeduplicatorFunction {
    public static final String CLASS_NAME = "com.google.common.collect.RegularImmutableList";
    private static final MethodHandle ARRAY_GETTER = MethodHandleHelper.findFieldGetter(CLASS_NAME, "array");
    private final Deduplicator parent;

    public RegularImmutableListDeduplicatorFunction(Deduplicator parent) {
        this.parent = parent;
    }

    @Override
    public Object deduplicate(Object o, int recursion) throws Throwable {
        Object[] array = (Object[]) ARRAY_GETTER.invoke(o);
        for (int i = 0; i < array.length; i++) {
            Object entry = array[i];
            Object entryD = parent.deduplicateObject(entry, recursion + 1);
            if (entryD != null) array[i] = entryD;
        }
        return o;
    }
}
