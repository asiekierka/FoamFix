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

package pl.asie.foamfix.client.condition;

import com.google.common.base.*;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.multipart.ConditionPropertyValue;
import scala.Enumeration;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class FoamyConditionPropertyValue extends ConditionPropertyValue {
    public static final class PredicateNegative implements Predicate<IBlockState> {
        private final IProperty<?> property;
        private final Object[] values;

        protected PredicateNegative(IProperty<?> property, Object[] values) {
            this.property = property;
            this.values = values;
        }

        @Override
        public boolean apply(@Nullable IBlockState state) {
            if (state != null) {
                Object value = state.getValue(property);
                for (int i = 0; i < values.length; i++) {
                    if (value.equals(values[i])) {
                        return false;
                    }
                }
            }

            return true;
        }

        public static final class HashingStrategy implements gnu.trove.strategy.HashingStrategy<PredicateNegative> {
            @Override
            public int computeHashCode(PredicateNegative object) {
                return 31 * object.property.hashCode() + Arrays.hashCode(object.values);
            }

            @Override
            public boolean equals(PredicateNegative o1, PredicateNegative o2) {
                if (o1.property == o2.property) {
                    if (o1.values.length != o2.values.length) return false;
                    for (int i = 0; i < o1.values.length; i++) {
                        if (o1.values[i] != o2.values[i]) return false;
                    }
                    return true;
                }
                return false;
            }
        }
    }

    public static final class PredicatePositive implements Predicate<IBlockState> {
        private final IProperty<?> property;
        private final Object[] values;

        protected PredicatePositive(IProperty<?> property, Object[] values) {
            this.property = property;
            this.values = values;
        }

        @Override
        public boolean apply(@Nullable IBlockState state) {
            if (state != null) {
                Object value = state.getValue(property);
                for (int i = 0; i < values.length; i++) {
                    if (value.equals(values[i])) {
                        return true;
                    }
                }
            }

            return false;
        }

        public static final class HashingStrategy implements gnu.trove.strategy.HashingStrategy<PredicatePositive> {
            @Override
            public int computeHashCode(PredicatePositive object) {
                return 31 * object.property.hashCode() + Arrays.hashCode(object.values);
            }

            @Override
            public boolean equals(PredicatePositive o1, PredicatePositive o2) {
                if (o1.property == o2.property) {
                    if (o1.values.length != o2.values.length) return false;
                    for (int i = 0; i < o1.values.length; i++) {
                        if (o1.values[i] != o2.values[i]) return false;
                    }
                    return true;
                }
                return false;
            }
        }
    }

    public static final class SingletonPredicatePositive implements Predicate<IBlockState> {
        private final IProperty<?> property;
        private final Object value;

        protected SingletonPredicatePositive(IProperty<?> property, Object value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public boolean apply(@Nullable IBlockState state) {
            return state != null && state.getValue(property).equals(value);
        }

        public static final class HashingStrategy implements gnu.trove.strategy.HashingStrategy<SingletonPredicatePositive> {
            @Override
            public int computeHashCode(SingletonPredicatePositive object) {
                return 31 * object.property.hashCode() + (object.value != null ? object.value.hashCode() : 0);
            }

            @Override
            public boolean equals(SingletonPredicatePositive o1, SingletonPredicatePositive o2) {
                return o1.property == o2.property && o1.value == o2.value;
            }
        }
    }

    public static final class SingletonPredicateNegative implements Predicate<IBlockState> {
        private final IProperty<?> property;
        private final Object value;

        protected SingletonPredicateNegative(IProperty<?> property, Object value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public boolean apply(@Nullable IBlockState state) {
            return state == null || !state.getValue(property).equals(value);
        }

        public static final class HashingStrategy implements gnu.trove.strategy.HashingStrategy<SingletonPredicateNegative> {
            @Override
            public int computeHashCode(SingletonPredicateNegative object) {
                return 31 * object.property.hashCode() + (object.value != null ? object.value.hashCode() : 0);
            }

            @Override
            public boolean equals(SingletonPredicateNegative o1, SingletonPredicateNegative o2) {
                return o1.property == o2.property && o1.value == o2.value;
            }
        }
    }

    private static final Splitter SPLITTER = Splitter.on('|').omitEmptyStrings();

    public FoamyConditionPropertyValue(String keyIn, String valueIn) {
        super(keyIn, valueIn);
    }

    @Override
    public Predicate<IBlockState> getPredicate(BlockStateContainer blockState) {
        final IProperty<?> property = blockState.getProperty(this.key);

        if (property == null) {
            throw new RuntimeException(this.toString() + ": Definition: " + blockState + " has no property: " + this.key);
        } else {
            String s = this.value;
            boolean negate = !s.isEmpty() && s.charAt(0) == '!';

            if (negate) {
                s = s.substring(1);
            }

            List<String> valueNames = SPLITTER.splitToList(s);
            if (valueNames.isEmpty()) {
                throw new RuntimeException(this.toString() + ": has an empty value: " + this.value);
            } else {
                final Object[] values = new Object[valueNames.size()];
                for (int i = 0; i < values.length; i++) {
                    Optional<?> valueParsed = property.parseValue(valueNames.get(i));

                    if (!valueParsed.isPresent()) {
                        throw new RuntimeException(this.toString() + ": has an unknown value: " + this.value);
                    } else {
                        values[i] = valueParsed.get();
                    }
                }

                if (negate) {
                    return values.length > 1 ? new PredicateNegative(property, values) : new SingletonPredicateNegative(property, values[0]);
                } else {
                    return values.length > 1 ? new PredicatePositive(property, values) : new SingletonPredicatePositive(property, values[0]);
                }
            }
        }
    }

    public static final class HashingStrategy implements gnu.trove.strategy.HashingStrategy<FoamyConditionPropertyValue> {
        @Override
        public int computeHashCode(FoamyConditionPropertyValue object) {
            return 31 * object.key.hashCode() + object.value.hashCode();
        }

        @Override
        public boolean equals(FoamyConditionPropertyValue o1, FoamyConditionPropertyValue o2) {
            return o2.key.equals(o1.key) && o2.value.equals(o1.value);
        }
    }
}
