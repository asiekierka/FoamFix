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

package pl.asie.foamfix.coremod.patches;

import net.minecraft.network.datasync.EntityDataManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.coremod.common.FoamyArrayBackedDataManagerMap;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;
import java.util.Map;

public class EntityDataManagerPatch implements TransformerFunction<ClassNode> {
    public static Map<Integer, EntityDataManager.DataEntry<?>> newArrayBackedMap() {
        return new FoamyArrayBackedDataManagerMap.OneTwelve<>();
    }

    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode node = it.next();
                if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESTATIC
                        && "com/google/common/collect/Maps".equals(((MethodInsnNode) node).owner)
                        && "newHashMap".equals(((MethodInsnNode) node).name)) {
                    AbstractInsnNode node2 = it.next();
                    if (node2 instanceof FieldInsnNode && node2.getOpcode() == Opcodes.PUTFIELD
                            && "net/minecraft/network/datasync/EntityDataManager".equals(((FieldInsnNode) node2).owner)
                            && ("entries".equals(((FieldInsnNode) node2).name)
                            || "field_187234_c".equals(((FieldInsnNode) node2).name))) {
                        while (!(it.previous() instanceof MethodInsnNode));
                        it.next();
                        it.set(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "pl/asie/foamfix/coremod/patches/EntityDataManagerPatch",
                                "newArrayBackedMap",
                                "()Ljava/util/Map;",
                                false
                        ));
                        it.next();
                        System.out.println("Replaced Maps.newHashMap() in " + classNode.name + " " + method.name);
                    }
                }
            }
        }
        return classNode;
    }
}
