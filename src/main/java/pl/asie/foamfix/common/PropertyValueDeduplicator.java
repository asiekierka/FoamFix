/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.foamfix.common;

import gnu.trove.set.hash.TCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.foamfix.client.IDeduplicatingStorage;
import pl.asie.foamfix.util.DeduplicatingStorageTrove;
import pl.asie.foamfix.util.HashingStrategies;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

public class PropertyValueDeduplicator {
    public int successfuls;

    @SuppressWarnings("unchecked")
    public void deduplicate() {
        IDeduplicatingStorage storage = new DeduplicatingStorageTrove(HashingStrategies.GENERIC);
        Set<IProperty> checkedProperties = new TCustomHashSet<>(HashingStrategies.IDENTITY);

        for (Block b : ForgeRegistries.BLOCKS) {
            // FIXME: Remove once Immersive Engineering fixes its stuff
            if (b.getRegistryName() == null || !("immersiveengineering".equals(b.getRegistryName().getNamespace()))) {
                for (IProperty property : b.getBlockState().getProperties()) {
                    try {
                        if (checkedProperties.add(property)) {
                            Collection allowedValues = property.getAllowedValues();
                            Collection newAllowedValues = (Collection) storage.deduplicate(allowedValues);
                            if (newAllowedValues != allowedValues) {
                                for (Field f : property.getClass().getDeclaredFields()) {
                                    f.setAccessible(true);
                                    Object o = f.get(property);
                                    if (o == allowedValues) {
                                        f.set(property, newAllowedValues);
                                        successfuls++;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
