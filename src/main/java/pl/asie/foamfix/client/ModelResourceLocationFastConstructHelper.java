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
package pl.asie.foamfix.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class ModelResourceLocationFastConstructHelper extends ModelResourceLocation {
    protected ModelResourceLocationFastConstructHelper(int unused, String... resourceName) {
        super(unused, resourceName);
    }

    public static String[] rlVariantToArray(ResourceLocation location, String variantIn) {
        if (location.getClass() == ResourceLocation.class) {
            return new String[]{location.getNamespace(), location.getPath(), variantIn};
        } else {
            return ModelResourceLocation.parsePathString(location + "#" + (variantIn == null ? "normal" : variantIn));
        }
    }
}