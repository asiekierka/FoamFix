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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PredicateNegative that = (PredicateNegative) o;
            if (property == that.property) {
                if (values.length != that.values.length) return false;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != that.values[i]) return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 31 * property.hashCode() + Arrays.hashCode(values);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PredicatePositive that = (PredicatePositive) o;
            if (property == that.property) {
                if (values.length != that.values.length) return false;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != that.values[i]) return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 31 * property.hashCode() + Arrays.hashCode(values);
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

                return negate ? new PredicateNegative(property, values) : new PredicatePositive(property, values);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FoamyConditionPropertyValue)) {
            return false;
        } else {
            FoamyConditionPropertyValue other = (FoamyConditionPropertyValue) obj;
            return other.key.equals(key) && other.value.equals(value);
        }
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + value.hashCode();
    }
}
