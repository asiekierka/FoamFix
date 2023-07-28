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

import gnu.trove.strategy.HashingStrategy;
import pl.asie.foamfix.client.FoamyMultipartBakedModel;

import java.util.Arrays;
import java.util.Objects;

public class FoamyMultipartBakedModelHashingStrategy implements HashingStrategy<FoamyMultipartBakedModel> {
    @Override
    public int computeHashCode(FoamyMultipartBakedModel object) {
        int hash = Arrays.hashCode(object.predicates);
        for (int i = 0; i < object.models.length; i++) {
            hash = hash * 31 + System.identityHashCode(object.models[i]);
        }
        return hash;
    }

    @Override
    public boolean equals(FoamyMultipartBakedModel o1, FoamyMultipartBakedModel o2) {
        if (!Arrays.equals(o1.predicates, o2.predicates)) return false;
        if (o1.models.length != o2.models.length) return false;
        for (int i = 0; i < o1.models.length; i++) {
            if (o1.models[i] != o2.models[i]) return false;
        }
        return true;
    }
}
