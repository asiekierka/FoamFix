/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
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

import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.foamfix.FoamFix;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.foamfix.ProxyClient;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.reflect.Field;
import java.util.Map;

public class FoamFixModelDeduplicate {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onModelBake(ModelBakeEvent event) {
        // TODO: analyze impact + obj. references
        // TODO: figure out why it breaks Botania (#1, refer to vazkii/botania/client/model/FloatingFlowerModel.java)
        // FoamUtils.wipeModelLoaderRegistryCache();

        if (FoamFixShared.config.geDeduplicate) {
            FoamFix.logger.info("Deduplicating models...");
            try {
                Map<ResourceLocation, IModel> cache = (Map<ResourceLocation, IModel>) MethodHandleHelper.findFieldGetter(ModelLoaderRegistry.class, "cache").invoke();
                if (cache != null) {
                    ProgressManager.ProgressBar bakeBar = ProgressManager.push("FoamFix: deduplicating", cache.size() + 2);

                    if (ProxyClient.deduplicator == null) {
                        ProxyClient.deduplicator = new Deduplicator();
                    }

                    ProxyClient.deduplicator.maxRecursion = FoamFixShared.config.clDeduplicateRecursionLevel;

                    ProxyClient.deduplicator.addObjects(ForgeRegistries.BLOCKS.getKeys());
                    ProxyClient.deduplicator.addObjects(ForgeRegistries.ITEMS.getKeys());

                    try {
                        bakeBar.step("Vertex formats");

                        for (Field f : DefaultVertexFormats.class.getDeclaredFields()) {
                            if (f.getType() == VertexFormat.class) {
                                f.setAccessible(true);
                                ProxyClient.deduplicator.deduplicateObject(f.get(null), 0);
                            }
                        }
                    } catch (Exception e) {

                    }

                    if (FoamFixShared.config.clDeduplicateIModels) {
                        for (ResourceLocation loc : cache.keySet()) {
                            IModel model = cache.get(loc);
                            String modelName = loc.toString();
                            bakeBar.step(String.format("[%s]", modelName));

                            try {
                                ProxyClient.deduplicator.addObject(loc);
                                ProxyClient.deduplicator.deduplicateObject(model, 0);
                            } catch (Exception e) {

                            }
                        }
                    }

                    try {
                        bakeBar.step("Stats");

                        for (Field f : StatList.class.getDeclaredFields()) {
                            if (f.getType() == StatBase[].class) {
                                f.setAccessible(true);
                                for (StatBase statBase : (StatBase[]) f.get(null)) {
                                    ProxyClient.deduplicator.deduplicateObject(statBase, 0);
                                }
                            }
                        }

                        for (StatBase statBase : StatList.ALL_STATS) {
                            ProxyClient.deduplicator.deduplicateObject(statBase, 0);
                        }
                    } catch (Exception e) {

                    }

                    ProgressManager.pop(bakeBar);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            ProgressManager.ProgressBar bakeBar = ProgressManager.push("FoamFix: deduplicating", event.getModelRegistry().getKeys().size());

            if (ProxyClient.deduplicator == null) {
                ProxyClient.deduplicator = new Deduplicator();
            }

            ProxyClient.deduplicator.maxRecursion = FoamFixShared.config.clDeduplicateRecursionLevel;
            FoamFix.logger.info("Deduplicating models...");

            for (ModelResourceLocation loc : event.getModelRegistry().getKeys()) {
                IBakedModel model = event.getModelRegistry().getObject(loc);
                String modelName = loc.toString();
                bakeBar.step(String.format("[%s]", modelName));

                if (model instanceof MultipartBakedModel) {
                    ProxyClient.deduplicator.successfuls++;
                    model = new FoamyMultipartBakedModel((MultipartBakedModel) model);
                }

                try {
                    ProxyClient.deduplicator.addObject(loc);
                    event.getModelRegistry().putObject(loc, (IBakedModel) ProxyClient.deduplicator.deduplicateObject(model, 0));
                } catch (Exception e) {

                }
            }

            ProgressManager.pop(bakeBar);
            FoamFix.logger.info("Deduplicated " + ProxyClient.deduplicator.successfuls + " (+ " + ProxyClient.deduplicator.successfulTrims + ") objects.");
            /* List<Class> map = Lists.newArrayList(ProxyClient.deduplicator.dedupObjDataMap.keySet());
            map.sort(Comparator.comparingInt(a -> ProxyClient.deduplicator.dedupObjDataMap.get(a)));
            for (Class c : map) {
                FoamFix.logger.info(c.getSimpleName() + " = " + ProxyClient.deduplicator.dedupObjDataMap.get(c));
            } */
        }

        ProxyClient.deduplicator = null; // release deduplicator to save memory
        FoamFix.updateRamSaved();
    }
}
