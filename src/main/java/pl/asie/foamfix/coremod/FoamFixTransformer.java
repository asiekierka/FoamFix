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
import java.util.Set;

import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassReader;
import com.google.common.io.ByteStreams;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;

public class FoamFixTransformer implements IClassTransformer
{
    public byte[] spliceMethods(final byte[] data, final String className, final String targetClassName, final String... methods) {
        try {
            final byte[] dataSplice = ByteStreams.toByteArray(this.getClass().getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class"));
            return spliceMethods(data, dataSplice, className, targetClassName, methods);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] spliceMethods(final byte[] data, final byte[] dataSplice, final String className, final String targetClassName, final String... methods) {
        final Set<String> methodSet = Sets.newHashSet(methods);

        final ClassReader readerData = new ClassReader(data);
        final ClassReader readerSplice = new ClassReader(dataSplice);
        final ClassWriter writer = new ClassWriter(8);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = targetClassName.replace('.', '/');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };
        ClassNode nodeData = new ClassNode();
        ClassNode nodeSplice = new ClassNode();
        readerData.accept(nodeData, 8);
        readerSplice.accept(new RemappingClassAdapter(nodeSplice, remapper), 8);
        for (int i = 0; i < nodeSplice.methods.size(); i++) {
            if (methodSet.contains(nodeSplice.methods.get(i).name)) {
                MethodNode mn = nodeSplice.methods.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.methods.size(); j++) {
                    if (nodeData.methods.get(j).name.equals(mn.name)
                            && nodeData.methods.get(j).desc.equals(mn.desc)) {
                        System.out.println("Spliced in: " + targetClassName + "." + mn.name);
                        nodeData.methods.set(j, mn);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    System.out.println("Added: " + targetClassName + "." + mn.name);
                    nodeData.methods.add(mn);
                    added = true;
                }
            }
        }
        nodeData.accept(writer);
        return writer.toByteArray();
    }

    public byte[] transform(final String name, final String transformedName, final byte[] dataOrig) {
        byte[] data = dataOrig;
        if (FoamFixShared.config.geBlockPosPatch) {
            if ("net.minecraft.util.math.Vec3i".equals(transformedName)) {
                data = BlockPosPatch.patchVec3i(data);
            } else {
                data = BlockPosPatch.patchOtherClass(data, "net.minecraft.util.math.BlockPos$MutableBlockPos".equals(transformedName));
            }
        }

        if (FoamFixShared.config.clBlockInfoPatch) {
            if ("net.minecraftforge.client.model.pipeline.BlockInfo".equals(transformedName)) {
                data = spliceMethods(data, "pl.asie.foamfix.coremod.BlockInfoPatch", transformedName, "updateLightMatrix");
            }
        }

        if (FoamFixShared.config.geSmallPropertyStorage) {
            if ("net.minecraft.block.state.BlockStateContainer".equals(transformedName)) {
                data = spliceMethods(data, "pl.asie.foamfix.common.FoamyBlockStateContainer", transformedName, "createState");
            }

            if ("net.minecraftforge.common.property.ExtendedBlockState".equals(transformedName)) {
                data = spliceMethods(data, "pl.asie.foamfix.common.FoamyExtendedBlockStateContainer", transformedName, "createState");
            }
        }

        if (FoamFixShared.config.geSmallLightingOptimize) {
            if ("net.minecraft.world.World".equals(transformedName)) {
                data = spliceMethods(data, "pl.asie.foamfix.coremod.WorldLightingPatch", transformedName, "checkLightFor");
            }
        }

        /* if ("net.minecraft.client.Minecraft".equals(transformedName)) {
            data = MinecraftPatch.patchMinecraft(data);
        } */
        return data;
    }
}
