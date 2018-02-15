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

package pl.asie.foamfix.coremod.patches;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.Opcodes;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;

/* Patch discovered by Aikar of PaperMC */
public class FastAirLookupPatch implements TransformerFunction<ClassNode> {
    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode node = it.next();
                if (node instanceof FieldInsnNode && node.getOpcode() == Opcodes.GETSTATIC
                        && "net/minecraft/init/Blocks".equals(((FieldInsnNode) node).owner)
                        && ("AIR".equals(((FieldInsnNode) node).name)
                        || "field_190931_a".equals(((FieldInsnNode) node).name))) {
                    AbstractInsnNode node2 = it.next();
                    if (node2 instanceof MethodInsnNode && node2.getOpcode() == Opcodes.INVOKESTATIC
                            && "net/minecraft/item/Item".equals(((MethodInsnNode) node2).owner)
                            && ("getItemFromBlock".equals(((MethodInsnNode) node2).name)
                            || "func_150898_a".equals(((MethodInsnNode) node2).name))) {
                        it.remove();
                        it.previous();
                        it.set(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "pl/asie/foamfix/FoamFix",
                                "getItemAir",
                                "()Lnet/minecraft/item/Item;",
                                false
                        ));
                        System.out.println("Replaced Item.getItemFromBlock(Blocks.AIR) in " + classNode.name + " " + method.name);
                    }
                }
            }
        }
        return classNode;
    }
}
