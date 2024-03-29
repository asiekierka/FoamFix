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
package pl.asie.foamfix.coremod;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.coremod.patches.*;
import pl.asie.foamfix.coremod.patches.network.DeflaterCompressionLevelPatch;
import pl.asie.foamfix.ghostbuster.GhostBusterDefinition;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.patchy.Patchy;
import pl.asie.patchy.TransformerHandler;
import pl.asie.patchy.handlers.TransformerHandlerByteArray;
import pl.asie.patchy.handlers.TransformerHandlerClassNode;
import pl.asie.patchy.handlers.TransformerHandlerClassVisitor;
import pl.asie.patchy.helpers.ConstructorReplacingTransformer;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class FoamFixTransformer implements IClassTransformer {
    private static byte[] getClassBytes(final String className) throws IOException {
        ClassLoader loader = FoamFixTransformer.class.getClassLoader();
        if (loader instanceof LaunchClassLoader) {
            return ((LaunchClassLoader) FoamFixTransformer.class.getClassLoader()).getClassBytes(className);
        } else {
            throw new RuntimeException("Incompatible class loader for FoamFixTransformer.getClassBytes: " + loader.getClass().getName());
        }
    }

    public static ClassNode spliceClasses(final ClassNode data, final String className, final boolean addMethods, final String... methods) {
        try {
            final byte[] dataSplice = getClassBytes(className);
            return spliceClasses(data, dataSplice, className, addMethods, methods);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassNode spliceClasses(final ClassNode nodeData, final byte[] dataSplice, final String className, final boolean addMethods, final String... methods) {
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
            MethodNode mn = nodeSplice.methods.get(i);
            if (methodSet.contains(mn.name)) {
                boolean added = false;

                for (int j = 0; j < nodeData.methods.size(); j++) {
                    MethodNode oldMn = nodeData.methods.get(j);
                    if (oldMn.name.equals(mn.name)
                            && oldMn.desc.equals(mn.desc)) {
                        System.out.println("Spliced in METHOD: " + targetClassName + "." + mn.name);
                        nodeData.methods.set(j, mn);
                        boolean isConstructor = oldMn.name.charAt(0) == '<';
                        if (nodeData.superName != null && nodeData.name.equals(nodeSplice.superName) && !isConstructor) {
                            ListIterator<AbstractInsnNode> nodeListIterator = mn.instructions.iterator();
                            while (nodeListIterator.hasNext()) {
                                AbstractInsnNode node = nodeListIterator.next();
                                if (node instanceof MethodInsnNode
                                        && node.getOpcode() == Opcodes.INVOKESPECIAL) {
                                    MethodInsnNode methodNode = (MethodInsnNode) node;
                                    if (targetClassName2.equals(methodNode.owner)) {
                                        methodNode.owner = nodeData.superName;
                                    }
                                }
                            }
                        }

                        if (!isConstructor) { // forbid <init>/<clinit>
                            oldMn.name = methodList.get((methodList.indexOf(oldMn.name)) & (~1)) + "_foamfix_old";
                            nodeData.methods.add(oldMn);
                        }
                        added = true;
                        break;
                    }
                }

                if (!added && addMethods) {
                    System.out.println("Added METHOD: " + targetClassName + "." + mn.name);
                    nodeData.methods.add(mn);
                    added = true;
                }
            }
        }

        for (int i = 0; i < nodeSplice.fields.size(); i++) {
            FieldNode mn = nodeSplice.fields.get(i);
            if (methodSet.contains(mn.name)) {
                boolean added = false;

                for (int j = 0; j < nodeData.fields.size(); j++) {
                    FieldNode otherNode = nodeData.fields.get(j);
                    if (otherNode.name.equals(mn.name)
                            && otherNode.desc.equals(mn.desc)) {
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
            final byte[] dataSplice = getClassBytes(className);
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
            FoamFixShared.emitWarningIfPresent("coremod.smallPropertyStorage", FoamFixShared::hasIdPatch, FoamFixShared.MOD_NAME_IDPATCH, false);

            patchy.addTransformerId("smallPropertyStorage_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.common.FoamyBlockStateContainer",
                    false, "createState", "createState", "foamfix_mapper", "foamfix_mapper"), "net.minecraft.block.state.BlockStateContainer");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.common.FoamyExtendedBlockStateContainer",
                    false, "createState", "createState", "foamfix_mapper", "foamfix_mapper"), "net.minecraftforge.common.property.ExtendedBlockState");
        }

        if (FoamFixShared.config.gePatchChunkSerialization) {
            boolean compatible = true;
            if (FoamFixShared.emitWarningIfPresent("coremod.patchChunkSerialization", FoamFixShared::hasIdPatch, FoamFixShared.MOD_NAME_IDPATCH, true))
                compatible = false;
            if (FoamFixShared.emitWarningIfPresent("coremod.patchChunkSerialization", FoamFixShared::hasSponge, FoamFixShared.MOD_NAME_SPONGE, true))
                compatible = false;

            if (compatible) {
                patchy.addTransformerId("patchChunkSerialization_v1");
                handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.BlockStateContainerSpongeInject",
                        false, "getSerializedSize", "func_186018_a"), "net.minecraft.world.chunk.BlockStateContainer");
            }
        }

        if (FoamFixShared.config.twImmediateLightingUpdates) {
            patchy.addTransformerId("immediateLightingUpdates_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.RenderGlobalImmediateInject",
                    false, "notifyLightSet","func_174959_b"), "net.minecraft.client.renderer.RenderGlobal");
        }

        if (FoamFixShared.config.clDynamicItemModels) {
            patchy.addTransformerId("dynamicItemModels_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.client.FoamFixDynamicItemModels",
		            false, "bake", "bake"), "net.minecraftforge.client.model.ItemLayerModel");
        }

        if (FoamFixShared.config.clCheapMinimumLighter) {
            patchy.addTransformerId("cheapMinimumLighter_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.BlockInfoInject",
                            true, "getRawB", "getRawB", "getRawS", "getRawS", "updateAO", "updateAO"), "net.minecraftforge.client.model.pipeline.BlockInfo");
        }

        if (FoamFixShared.config.geBlockPosPatch) {
            boolean compatible = true;
            if (FoamFixShared.emitWarningIfPresent("coremod.optimizedBlockPos", FoamFixShared::hasSponge, "SpongeForge", true))
                compatible = false;

            if (compatible) {
                patchy.addTransformerId("blockPosPatch_v1");
                handlerCN.add(BlockPosPatch::patchVec3i, "net.minecraft.util.math.Vec3i");
                handlerCV.add(BlockPosPatch::patchOtherClass);
            }
        }

        if (FoamFixShared.config.geFasterEntityLookup) {
            patchy.addTransformerId("fasterClassInheritanceMultiMap_v1");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.util.ClassInheritanceMultiMap", "pl.asie.foamfix.coremod.common.FoamyClassInheritanceMultiMap", "<init>"),
                    "net.minecraft.world.chunk.Chunk");
        }

        if (FoamFixShared.config.clSmallModelConditions) {
            patchy.addTransformerId("smallModelConditions_v2");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.client.renderer.block.model.multipart.ConditionPropertyValue", "pl.asie.foamfix.client.condition.FoamyConditionPropertyValue"),
                    "net.minecraft.client.renderer.block.model.multipart.Selector$Deserializer");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.client.renderer.block.model.multipart.ConditionOr", "pl.asie.foamfix.client.condition.FoamyConditionOr"),
                    "net.minecraft.client.renderer.block.model.multipart.Selector$Deserializer");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.client.renderer.block.model.multipart.ConditionAnd", "pl.asie.foamfix.client.condition.FoamyConditionAnd"),
                    "net.minecraft.client.renderer.block.model.multipart.Selector$Deserializer");
        }

        if (FoamFixShared.config.clOpenUrlLinux) {
            patchy.addTransformerId("openUrlLinux_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.GuiScreenLinuxInject",
                    false, "openWebLink", "func_175282_a"));
        }

        if (FoamFixShared.config.neMicroOptimizations) {
            patchy.addTransformerId("neMicroOptimizations_writeString_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.network.PacketBufferInject",
                    false, "writeString", "func_180714_a"), "net.minecraft.network.PacketBuffer");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.network.ByteBufUtilsInject",
                    false, "writeUTF8String"), "net.minecraftforge.fml.common.network.ByteBufUtils");
        }

        if (FoamFixShared.neDeflaterCompression >= 0) {
            patchy.addTransformerId("neDeflaterCompression");
            handlerCN.add(new DeflaterCompressionLevelPatch(), "net.minecraft.network.NettyCompressionEncoder");
        }

        if (FoamFixShared.config.geFasterAirLookup) {
            patchy.addTransformerId("fasterAirLookup_v1");
            handlerCN.add(new FastAirLookupPatch(), "net.minecraft.item.ItemStack");
        }

        if (FoamFixShared.config.geFasterPropertyComparisons) {
            patchy.addTransformerId("fasterPropertyComparisons_v2");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.PropertyFasterComparisonsInject$Bool",
                    true, "equals", "equals", "hashCode", "hashCode"), "net.minecraft.block.properties.PropertyBool");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.PropertyFasterComparisonsInject$Enum",
                    false, "equals", "equals"), "net.minecraft.block.properties.PropertyEnum");
            for (String s : new String[] { "net.minecraft.block.properties.PropertyInteger", "net.minecraft.block.properties.PropertyEnum" }) {
                handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.CachingHashCodeInject",
		                true, "hashCode", "hashCode", "foamfix_hashCode", "foamfix_hashCode", "foamfix_hashCode_calced", "foamfix_hashCode_calced"),
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

        if (FoamFixShared.config.geFasterHopper) {
            patchy.addTransformerId("fasterHopper_v1");
            handlerCV.add(new ConstructorReplacingTransformer("net.minecraft.tileentity.TileEntityHopper", "pl.asie.foamfix.common.TileEntityFasterHopper", "createNewTileEntity", "func_149915_a"),
                    "net.minecraft.block.BlockHopper");

            patchy.addTransformerId("tileEntityGetKeyWrap_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.TileEntityGetKeyWrapInject",
                    false, "getKey", "func_190559_a"), "net.minecraft.tileentity.TileEntity");
        }

        if (FoamFixShared.config.geFixWorldEntityCleanup) {
            patchy.addTransformerId("fixWorldEntityCleanup_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.WorldRemovalInject",
                    true, "foamfix_removeUnloadedEntities", "foamfix_removeUnloadedEntities"), "net.minecraft.world.World");
            handlerCN.add(new WorldServerRemovalPatch(), "net.minecraft.world.WorldServer");
        }

        if (FoamFixShared.config.txEnable) {
            patchy.addTransformerId("fastTextureAtlasSprite_v1");
            handlerCV.add(new ConstructorReplacingTransformer(
                    "net.minecraft.client.renderer.texture.TextureAtlasSprite",
                    "pl.asie.foamfix.client.FastTextureAtlasSprite",
                    "makeAtlasSprite", "func_176604_a"
            ), "net.minecraft.client.renderer.texture.TextureAtlasSprite");
        }

        if (FoamFixShared.config.geMobSpawnerCheckSpeed > 2) {
            patchy.addTransformerId("geMobSpawnerCheckSpeed_v2");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.CachingMobSpawnerLogicInject",
                    false, "isActivated", "func_98279_f",
                    "foamfix_activatedCache", "foamfix_activatedCache",
                    "foamfix_activatedCachePESize", "foamfix_activatedCachePESize",
                    "foamfix_activatedCacheTime", "foamfix_activatedCacheTime",
                    "foamfix_forcedCache", "foamfix_forcedCache",
                    "foamfix_forcedCacheTime", "foamfix_forcedCacheTime"
            ), "net.minecraft.tileentity.MobSpawnerBaseLogic");
        }

        if (FoamFixShared.config.gbPatchBeds) {
            patchy.addTransformerId("gbPatchBeds_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.BlockBedInject",
                    false, "neighborChanged", "func_189540_a"), "net.minecraft.block.BlockBed");
        }

        if (FoamFixShared.config.gbPatchFluids) {
            patchy.addTransformerId("gbPatchFluids_v2");
            // TODO: The ChunkProvider lookup overhead concerns me, even if technically more suitable.
            /* handlerCN.add(new GhostBusterDefinitionPatch(new GhostBusterDefinition("canFlowInto", "canFlowInto", 1, 2, 0, false)),
                    "net.minecraftforge.fluids.BlockFluidClassic"); */
            handlerCN.add(new GhostBusterDefinitionPatch(GhostBusterDefinition.updateTick(4)),
                    "net.minecraftforge.fluids.BlockFluidClassic");
            handlerCN.add(new GhostBusterDefinitionPatch(GhostBusterDefinition.updateTick(1)),
                    "net.minecraftforge.fluids.BlockFluidFinite");
        }

        if (FoamFixShared.config.gbPatchBopGrass) {
            patchy.addTransformerId("gbPatchBopGrass_v1");
            handlerCN.add(new GhostBusterDefinitionPatch(new GhostBusterDefinition("spreadGrass", "spreadGrass", 1, 2, 3)),
                    "biomesoplenty.common.block.BlockBOPGrass");
        }

        if (FoamFixShared.config.gbPatchFarmland) {
            patchy.addTransformerId("gbPatchFarmland_v1");
            // TODO: Could be more efficient by making a custom BlockPos iterator.
            handlerCV.add(new GhostBusterBlockStateAccessPatch("hasWater", "func_176530_e"), "net.minecraft.block.BlockFarmland");
        }

        if (FoamFixShared.config.clJeiCreativeSearch) {
            patchy.addTransformerId("clJeiCreativeSearch_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.patches.jei.SearchTreeJEIManagerInject",
                    false, "onResourceManagerReload", "func_110549_a"),
                    "net.minecraft.client.util.SearchTreeManager");
        }

        if (FoamFixShared.config.clWipeModelCache) {
            patchy.addTransformerId("wipeModelCache_v1");
            handlerCN.add(data -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.VanillaModelWrapperInject",
                    false,"bakeNormal", "bakeNormal"),
                    "net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
        }

        if (FoamFixShared.config.clClearCachesOnUnload) {
            patchy.addTransformerId("clearCachesOnUnload_v2");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.AnimationModelBaseClearCacheInject",
                    false, "render", "render"), "net.minecraftforge.client.model.animation.AnimationModelBase");
        }

        //if (FoamFixShared.config.clClearCachesOnUnload) {
        {
            patchy.addTransformerId("modelResourceLocationFastConstruct_v1");
            handlerCN.add((data) -> spliceClasses(data, "pl.asie.foamfix.coremod.injections.client.ModelResourceLocationFastConstructInject",
                    false, "<init>", "<init>"), "net.minecraft.client.renderer.block.model.ModelResourceLocation");
        }

        if (FoamFixShared.config.gbCustomRules != null && FoamFixShared.config.gbCustomRules.length > 0) {
            for (String s : FoamFixShared.config.gbCustomRules) {
                String[] sSplit = s.split(";");
                try {
                    GhostBusterDefinition definition = new GhostBusterDefinition(
                            sSplit[1],
                            sSplit[1],
                            Integer.parseInt(sSplit[2]),
                            Integer.parseInt(sSplit[3]),
                            Integer.parseInt(sSplit[4])
                    );
                    handlerCN.add(new GhostBusterDefinitionPatch(definition), sSplit[0]);
                } catch (Throwable t) {
                    throw new RuntimeException("Could not parse custom rule: " + s, t);
                }
            }
        }

        if (FoamFixShared.config.clModelLoaderCleanup) {
            patchy.addTransformerId("modelLoaderCleanup_v1");
            handlerCN.add(new ModelLoaderCleanupPatch(), "net.minecraft.client.renderer.block.model.ModelManager");
        }
    }

    public byte[] transform(final String name, final String transformedName, final byte[] dataOrig) {
        return FoamFixShared.isCoremod ? patchy.transform(name, transformedName, dataOrig) : dataOrig;
    }
}
