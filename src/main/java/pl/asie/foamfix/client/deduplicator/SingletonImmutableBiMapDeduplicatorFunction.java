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
package pl.asie.foamfix.client.deduplicator;

import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;

public class SingletonImmutableBiMapDeduplicatorFunction implements DeduplicatorFunction {
    public static final String CLASS_NAME = "com.google.common.collect.SingletonImmutableBiMap";
    private static final MethodHandle KEY_GETTER = MethodHandleHelper.findFieldGetter(CLASS_NAME, "singleKey");
    private static final MethodHandle KEY_SETTER = MethodHandleHelper.findFieldSetter(CLASS_NAME, "singleKey");
    private static final MethodHandle VALUE_GETTER = MethodHandleHelper.findFieldGetter(CLASS_NAME, "singleValue");
    private static final MethodHandle VALUE_SETTER = MethodHandleHelper.findFieldSetter(CLASS_NAME, "singleValue");
    private final Deduplicator parent;

    public SingletonImmutableBiMapDeduplicatorFunction(Deduplicator parent) {
        this.parent = parent;
    }

    @Override
    public Object deduplicate(Object o, int recursion) throws Throwable {
        {
            Object element = KEY_GETTER.invoke(o);
            Object elementD = parent.deduplicateObject(element, recursion + 1);
            if (elementD != null) KEY_SETTER.invoke(o, elementD);
        }
        {
            Object element = VALUE_GETTER.invoke(o);
            Object elementD = parent.deduplicateObject(element, recursion + 1);
            if (elementD != null) VALUE_SETTER.invoke(o, elementD);
        }
        return o;
    }
}
