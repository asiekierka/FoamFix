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
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package pl.asie.foamfix.coremod.common;

import com.google.common.collect.Iterators;
import net.minecraft.util.ClassInheritanceMultiMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FoamyClassInheritanceMultiMap<T> extends ClassInheritanceMultiMap<T> {
    public FoamyClassInheritanceMultiMap(Class<T> p_i45909_1_) {
        super(p_i45909_1_);
    }

    public <S> Iterable<S> getByClass(final Class<S> p_getByClass_1_) {
        return new Iterable<S>() {
            public Iterator<S> iterator() {
                Class c = FoamyClassInheritanceMultiMap.this.initializeClassLookup(p_getByClass_1_);
                List<T> list = FoamyClassInheritanceMultiMap.this.map.get(c);
                if(list == null) {
                    return Collections.emptyIterator();
                } else {
                    Iterator<T> iterator = list.iterator();
                    return p_getByClass_1_ == c ? (Iterator<S>) iterator : Iterators.filter(iterator, p_getByClass_1_);
                }
            }
        };
    }
}
