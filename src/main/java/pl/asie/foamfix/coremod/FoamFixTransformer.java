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
package pl.asie.foamfix.coremod;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.ClassReader;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.coremod.patches.*;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.patchy.*;
import pl.asie.patchy.handlers.*;
import pl.asie.patchy.helpers.ConstructorReplacingTransformer;

public class FoamFixTransformer implements IClassTransformer {
    public static ClassNode spliceClasses(final ClassNode data, final String className, final String... methods) {
        try {
            final byte[] dataSplice = ((LaunchClassLoader) FoamFixTransformer.class.getClassLoader()).getClassBytes(className);
            return spliceClasses(data, dataSplice, className, methods);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassNode spliceClasses(final ClassNode nodeData, final byte[] dataSplice, final String className, final String... methods) {
        // System.out.println("Splicing from " + className + " to " + targetClassName)
        if (dataSplice == null) {
            throw new RuntimeException("Class " + className + " not found! This is a FoamFix bug!");
        }

        final Set<String> methodSet = Sets.newHashSet(methods);
        final List<String> methodList = Lists.newArrayList(methods);

        final ClassReader readerSplice = new ClassReader(dataSplice);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = nodeData.name;
        final String targetClassName = targetClassName2.replace('/', '.');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };

        ClassNode nodeSplice = new ClassNode();
        readerSplice.accept(new ClassRemapper(nodeSplice, remapper), ClassReader.EXPAND_FRAMES);
        for (String s : nodeSplice.interfaces) {
            if (s.contains("IFoamFix")) {
                nodeData.interfaces.add(s);
                System.out.println("Added INTERFACE: " + s);
            }
        }

        for (int i = 0; i < nodeSplice.methods.size(); i++) {
            if (methodSet.contains(nodeSplice.methods.get(i).name)) {
                MethodNode mn = nodeSplice.methods.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.methods.size(); j++) {
                    if (nodeData.methods.get(j).name.equals(mn.name)
                            && nodeData.methods.get(j).desc.equals(mn.desc)) {
                        MethodNode oldMn = nodeData.methods.get(j);
                        System.out.println("Spliced in METHOD: " + targetClassName + "." + mn.name);
                        nodeData.methods.set(j, mn);
                        oldMn.name = methodList.get((methodList.indexOf(oldMn.name)) & (~1)) + "_foamfix_old";
                        nodeData.methods.add(oldMn);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    System.out.println("Added METHOD: " + targetClassName + "." + mn.name);
                    nodeData.methods.add(mn);
                    added = true;
                }
            }
        }

        for (int i = 0; i < nodeSplice.fields.size(); i++) {
            if (methodSet.contains(nodeSplice.fields.get(i).name)) {
                FieldNode mn = nodeSplice.fields.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.fields.size(); j++) {
                    if (nodeData.fields.get(j).name.equals(mn.name)
                            && nodeData.fields.get(j).desc.equals(mn.desc)) {
                        System.out.println("Spliced in FIELD: " + targetClassName + "." + mn.name);
                        nodeData.fields.set(j, mn);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    System.out.println("Added FIELD: " + targetClassName + "." + mn.name);
                    nodeData.fields.add(mn);
                    added = true;
                }
            }
        }

        return nodeData;
    }

    public static ClassNode replaceClasses(final ClassNode data, final String className) {
        try {
            final byte[] dataSplice = ((LaunchClassLoader) FoamFixTransformer.class.getClassLoader()).getClassBytes(className);
            return replaceClasses(data, dataSplice, className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassNode replaceClasses(final ClassNode nodeData, final byte[] dataSplice, final String className) {
        System.out.println("replacing " + nodeData.name + " with " + className);
        if (dataSplice == null) {
            throw new RuntimeException("Class " + className + " not found! This is a FoamFix bug!");
        }

        final ClassReader readerSplice = new ClassReader(dataSplice);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = nodeData.name;
        final String targetClassName = targetClassName2.replace('/', '.');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };

        ClassNode nodeSplice = new ClassNode();
        readerSplice.accept(new ClassRemapper(nodeSplice, remapper), ClassReader.EXPAND_FRAMES);
        return nodeSplice;
    }

    private static final Patchy patchy = new Patchy();

    public static void init() {
        patchy.registerHandler(byte[].class, new TransformerHandlerByteArray(patchy));
        patchy.registerHandler(ClassNode.class, new TransformerHandlerClassNode(patchy));
        patchy.registerHandler(ClassVisitor.class, new TransformerHandlerClassVisitor(patchy));

        TransformerHandler<byte[]> handler = patchy.getHandler(byte[].class);
        TransformerHandler<ClassNode> handlerCN = patchy.getHandler(ClassNode.class);
        TransformerHandler<ClassVisitor> handlerCV = patchy.getHandler(ClassVisitor.class);

        if (FoamFixShared.config.geSmallPropertyStorage) {
            patchy.addTransformerId("smallPropertyStorage_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.common.FoamyBlockStateContainer",
                    "createState", "createState"), "net.minecraft.block.state.BlockStateContainer");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.common.FoamyExtendedBlockStateContainer",
                    "createState", "createState"), "net.minecraftforge.common.property.ExtendedBlockState");
        }

        /* if (FoamFixShared.config.geSmallLightingOptimize) {
            transformFunctions.put("net.minecraft.world.World", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.WorldLightingPatch", transformedName,
                            "checkLightFor","func_180500_c");
                }
            });
        } */

        if (FoamFixShared.config.twImmediateLightingUpdates) {
            patchy.addTransformerId("immediateLightingUpdates_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.RenderGlobalImmediateInject",
                    "notifyLightSet","func_174959_b"), "net.minecraft.client.renderer.RenderGlobal");
        }

        if (FoamFixShared.config.clDynamicItemModels) {
            patchy.addTransformerId("dynamicItemModels_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.client.FoamFixDynamicItemModels",
                    "bake", "bake"), "net.minecraftforge.client.model.ItemLayerModel");
        }

        if (FoamFixShared.config.clParallelModelBaking) {
            patchy.addTransformerId("parallelModelBaking_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.ModelBakeryParallelInject",
                    "setupModelRegistry", "func_177570_a"), "net.minecraftforge.client.model.ModelLoader");
        }

        if (FoamFixShared.config.clFasterVertexLighter) {
            patchy.addTransformerId("fasterVertexLighter_v2");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.BlockInfoInject",
                            "getRawB", "getRawB", "getRawS", "getRawS", "updateRawBS", "updateRawBS"), "net.minecraftforge.client.model.pipeline.BlockInfo");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.VertexLighterFlatInject",
                    "updateLightmap", "updateLightmap", "updateBlockInfo", "updateBlockInfo"),
                    "net.minecraftforge.client.model.pipeline.VertexLighterFlat");
        }

        if (FoamFixShared.config.geBlockPosPatch) {
            patchy.addTransformerId("blockPosPatch_v1");
            handlerCN.add(BlockPosPatch::patchVec3i, "net.minecraft.util.math.Vec3i");
            handlerCV.add(BlockPosPatch::patchOtherClass);
        }

        if (FoamFixShared.config.geFasterEntityLookup) {
            patchy.addTransformerId("fasterClassInheritanceMultiMap_v1");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.util.ClassInheritanceMultiMap", "pl.asie.foamfix.coremod.common.FoamyClassInheritanceMultiMap", "<init>"),
                    "net.minecraft.world.chunk.Chunk");
        }

        if (FoamFixShared.config.geFasterAirLookup) {
            patchy.addTransformerId("fasterAirLookup_v1");
            handlerCN.add(new FastAirLookupPatch(), "net.minecraft.item.ItemStack");
        }

        if (FoamFixShared.config.geFasterPropertyComparisons) {
            patchy.addTransformerId("fasterPropertyComparisons_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.PropertyFasterComparisonsInject$Bool",
                    "equals", "equals", "hashCode", "hashCode"), "net.minecraft.block.properties.PropertyBool");
            for (String s : new String[] { "net.minecraft.block.properties.PropertyInteger", "net.minecraft.block.properties.PropertyEnum" }) {
                handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.CachingHashCodeInject",
                        "hashCode", "hashCode", "foamfix_hashCode", "foamfix_hashCode", "foamfix_hashCode_calced", "foamfix_hashCode_calced"),
                        s);
            }
        }

        if (FoamFixShared.config.geFasterEntityDataManager) {
            patchy.addTransformerId("fasterEntityDataManager_v1");
            handlerCN.add(new EntityDataManagerPatch(), "net.minecraft.network.datasync.EntityDataManager");
        }

        patchy.addTransformerId("disableTextureAnimations_v1");
        handlerCN.add(new ReturnIfBooleanTruePatch("clDisableTextureAnimations", "updateAnimations", "func_94248_c"),
                "net.minecraft.client.renderer.texture.TextureMap");

        if (FoamFixShared.config.geFixUnnecessaryGhostload) {
            patchy.addTransformerId("fixUnnecessaryGhostload_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.BlockBedInject",
                    "func_190524_a", "neighborChanged"), "net.minecraft.block.BlockBed");
        }

        if (FoamFixShared.config.geFasterHopper) {
            patchy.addTransformerId("fasterHopper_v1");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.tileentity.TileEntityHopper", "pl.asie.foamfix.common.TileEntityFasterHopper", "createNewTileEntity", "func_149915_a"),
                    "net.minecraft.block.BlockHopper");

            patchy.addTransformerId("tileEntityGetKeyWrap_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.TileEntityGetKeyWrapInject",
                    "getKey", "func_190559_a"), "net.minecraft.tileentity.TileEntity");
        }

        if (FoamFixShared.config.geFixWorldEntityCleanup) {
            patchy.addTransformerId("fixWorldEntityCleanup_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.WorldRemovalInject",
                    "foamfix_removeUnloadedEntities", "foamfix_removeUnloadedEntities"), "net.minecraft.world.World");
            handlerCN.add(new WorldServerRemovalPatch(), "net.minecraft.world.WorldServer");
        }

        if (FoamFixShared.config.staging4305) {
            patchy.addTransformerId("staging4305_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.staging.Patch4305",
                    "diffuseLight", "diffuseLight"), "net.minecraftforge.client.model.pipeline.LightUtil");
        }

        if (FoamFixShared.config.txEnable) {
            patchy.addTransformerId("fastTextureAtlasSprite_v1");
            handlerCV.add(new ConstructorReplacingTransformer(
                    "net.minecraft.client.renderer.texture.TextureAtlasSprite",
                    "pl.asie.foamfix.client.FastTextureAtlasSprite",
                    "makeAtlasSprite", "func_176604_a"
            ), "net.minecraft.client.renderer.texture.TextureAtlasSprite");
        }

        /* if (FoamFixShared.config.staging4370) {
            patchy.addTransformerId("staging4370_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.CachingHashCodeInject",
                    "hashCode", "hashCode", "foamfix_hashCode", "foamfix_hashCode", "foamfix_hashCode_calced", "foamfix_hashCode_calced"),
                    "net.minecraft.client.renderer.vertex.VertexFormat");
        } */
    }

    public byte[] transform(final String name, final String transformedName, final byte[] dataOrig) {
        return FoamFixShared.isCoremod ? patchy.transform(name, transformedName, dataOrig) : dataOrig;
    }
}
