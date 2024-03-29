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
package pl.asie.foamfix.client.condition;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.multipart.ICondition;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class FoamyConditionAnd implements ICondition {
    public static final class PredicateImpl implements Predicate<IBlockState> {
        private final Predicate[] predicates;

        public PredicateImpl(Predicate[] predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean apply(@Nullable IBlockState input) {
            for (int i = 0; i < predicates.length; i++) {
                //noinspection unchecked
                if (!predicates[i].apply(input)) {
                    return false;
                }
            }
            return true;
        }

        public static final class HashingStrategy implements gnu.trove.strategy.HashingStrategy<PredicateImpl> {
            @Override
            public int computeHashCode(PredicateImpl object) {
                return Arrays.hashCode(object.predicates);
            }

            @Override
            public boolean equals(PredicateImpl o1, PredicateImpl o2) {
                return Arrays.equals(o1.predicates, o2.predicates);
            }
        }
    }

    private final Iterable<ICondition> conditions;

    public FoamyConditionAnd(Iterable<ICondition> conditionsIn)
    {
        this.conditions = conditionsIn;
    }

    @Override
    public Predicate<IBlockState> getPredicate(final BlockStateContainer blockState) {
        Predicate[] predicates = StreamSupport.stream(conditions.spliterator(), false)
                .map(cond -> cond == null ? null : cond.getPredicate(blockState))
                .toArray(Predicate[]::new);

        if (predicates.length <= 1) {
            return predicates[0];
        } else {
            return new PredicateImpl(predicates);
        }
    }
}
