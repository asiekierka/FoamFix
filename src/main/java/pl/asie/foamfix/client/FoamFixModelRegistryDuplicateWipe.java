/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
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
package pl.asie.foamfix.client;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.ProxyClient;

import java.lang.reflect.Field;
import java.util.Map;

public class FoamFixModelRegistryDuplicateWipe {
    @SubscribeEvent
    public void onTextureStitchPost(TextureStitchEvent.Post event) {
        ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        BlockModelShapes bms = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        ModelManager mgr = bms.getModelManager();

        Field f = ObfuscationReflectionHelper.findField(ModelManager.class, "field_174958_a");
        try {
            IRegistry<ModelResourceLocation, IBakedModel> registry = (IRegistry<ModelResourceLocation, IBakedModel>) f.get(mgr);
            FoamFix.logger.info("Clearing unnecessary model registry of size " + registry.getKeys().size() + ".");
            for (ModelResourceLocation l : registry.getKeys()) {
                registry.putObject(l, ProxyClient.DUMMY_MODEL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        f = ObfuscationReflectionHelper.findField(BlockModelShapes.class, "field_178129_a");
        try {
            Map<IBlockState, IBakedModel> modelStore = (Map<IBlockState, IBakedModel>) f.get(bms);
            FoamFix.logger.info("Clearing unnecessary model store of size " + modelStore.size() + ".");
            modelStore.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imm instanceof ItemModelMesherForge) {
            f = ReflectionHelper.findField(ItemModelMesherForge.class, "models");
            try {
                Map<IRegistryDelegate<Item>, TIntObjectHashMap<IBakedModel>> modelStore = (Map<IRegistryDelegate<Item>, TIntObjectHashMap<IBakedModel>>) f.get(imm);
                FoamFix.logger.info("Clearing unnecessary item shapes cache of size " + modelStore.size() + ".");
                modelStore.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
