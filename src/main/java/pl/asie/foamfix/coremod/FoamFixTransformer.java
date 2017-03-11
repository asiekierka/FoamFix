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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassReader;
import com.google.common.io.ByteStreams;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import pl.asie.foamfix.client.FoamyDefaultResourcePack;
import pl.asie.foamfix.shared.FoamFixShared;

import javax.annotation.Nullable;

public class FoamFixTransformer implements IClassTransformer
{
    // TODO: NEW, INVOKESPECIAL.<init> PATCHER
    public static byte[] replaceConstructor(final byte[] data, final String className, final String from, final String to, final String... methods) {
        final ClassReader reader = new ClassReader(data);
        final ClassWriter writer = new ClassWriter(0);
        reader.accept(new FoamFixConstructorReplacer(from, to, methods).getClassVisitor(Opcodes.ASM5, writer), 0);
        return writer.toByteArray();
    }

    public static byte[] spliceClasses(final byte[] data, final String className, final String targetClassName, final String... methods) {
        try {
            final byte[] dataSplice = ((LaunchClassLoader) FoamFixTransformer.class.getClassLoader()).getClassBytes(className);
            return spliceClasses(data, dataSplice, className, targetClassName, methods);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] spliceClasses(final byte[] data, final byte[] dataSplice, final String className, final String targetClassName, final String... methods) {
        // System.out.println("Splicing from " + className + " to " + targetClassName);

        final Set<String> methodSet = Sets.newHashSet(methods);
        final List<String> methodList = Lists.newArrayList(methods);

        final ClassReader readerData = new ClassReader(data);
        final ClassReader readerSplice = new ClassReader(dataSplice);
        final ClassWriter writer = new ClassWriter(0);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = targetClassName.replace('.', '/');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };

        ClassNode nodeData = new ClassNode();
        ClassNode nodeSplice = new ClassNode();
        readerData.accept(nodeData, 0);
        readerSplice.accept(new RemappingClassAdapter(nodeSplice, remapper), ClassReader.EXPAND_FRAMES);
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
        nodeData.accept(writer);
        return writer.toByteArray();
    }

    private static final Multimap<String, TransformerFunction> transformFunctions = HashMultimap.create();

    public static void init() {
        if (FoamFixShared.config.clBlockInfoPatch) {
            transformFunctions.put("net.minecraftforge.client.model.pipeline.BlockInfo", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.coremod.blockinfo.BlockInfoPatch", transformedName,
                            "updateLightMatrix", "updateLightMatrix");
                }
            });
        }

        if (FoamFixShared.config.geSmallPropertyStorage) {
            transformFunctions.put("net.minecraft.block.state.BlockStateContainer", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.common.FoamyBlockStateContainer", transformedName,
                            "createState", "createState");
                }
            });

            transformFunctions.put("net.minecraftforge.common.property.ExtendedBlockState", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.common.FoamyExtendedBlockStateContainer", transformedName,
                            "createState", "createState");
                }
            });
        }

        if (FoamFixShared.config.geSmallLightingOptimize) {
            transformFunctions.put("net.minecraft.world.World", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.coremod.WorldLightingPatch", transformedName,
                            "checkLightFor","func_180500_c");
                }
            });
        }

        if (FoamFixShared.config.geImmediateLightingUpdates) {
            transformFunctions.put("net.minecraft.client.renderer.RenderGlobal", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.coremod.RenderGlobalImmediatePatch", transformedName,
                            "notifyLightSet","func_174959_b");
                }
            });
        }

        if (FoamFixShared.config.clDynamicItemModels) {
            transformFunctions.put("net.minecraftforge.client.model.ItemLayerModel", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.client.FoamFixDynamicItemModels", transformedName,
                            "bake", "bake");
                }
            });
        }

        if (FoamFixShared.config.clFasterResourceLoading > 0) {
            final String className = FoamyDefaultResourcePack.getClassName();
            transformFunctions.put("net.minecraft.client.Minecraft", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return replaceConstructor(data, transformedName, "net.minecraft.client.resources.DefaultResourcePack",
                            className, "<init>");
                }
            });
        }

        if (FoamFixShared.config.shModelLoaderFirstPass) {
            transformFunctions.put("net.minecraftforge.client.model.ModelLoader", new TransformerFunction() {
                @Override
                public byte[] transform(byte[] data, String transformedName) {
                    return spliceClasses(data, "pl.asie.foamfix.coremod.hacks.ModelLoaderSpeedhack", transformedName,
                            "loadBlock", "loadBlock");
                }
            });
        }
    }

    public byte[] transform(final String name, final String transformedName, final byte[] dataOrig) {
        if (dataOrig == null)
            return null;

        byte[] data = dataOrig;

        if (FoamFixShared.config.geBlockPosPatch) {
            if ("net.minecraft.util.math.Vec3i".equals(transformedName)) {
                data = BlockPosPatch.patchVec3i(data);
            } else {
                data = BlockPosPatch.patchOtherClass(data, "net.minecraft.util.math.BlockPos$MutableBlockPos".equals(transformedName));
            }
        }

        for (TransformerFunction function : transformFunctions.get(transformedName)) {
            data = function.transform(data, transformedName);
        }

        return data;
    }
}
