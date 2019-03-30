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

package pl.asie.foamfix.coremod.injections.crafting;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;

public class ContainerPatchCrafting implements TransformerFunction<ClassNode> {
    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("slotChangedCraftingGrid") || method.name.equals("func_192389_a")) {
                ListIterator<AbstractInsnNode> it = method.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESTATIC
                            && "net/minecraft/item/crafting/CraftingManager".equals(((MethodInsnNode) node).owner)
                            && (
                            "findMatchingRecipe".equals(((MethodInsnNode) node).name)
                            || "func_192413_b".equals(((MethodInsnNode) node).name)
                            )) {
                        ((MethodInsnNode) node).owner = "pl/asie/foamfix/coremod/injections/crafting/CraftingOptHook";
                        ((MethodInsnNode) node).name = "findMatchingRecipe";
                        ((MethodInsnNode) node).desc = "(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;Lnet/minecraft/inventory/InventoryCraftResult;)Lnet/minecraft/item/crafting/IRecipe;";
                        it.previous();
                        it.add(new VarInsnNode(Opcodes.ALOAD, 4));
                        System.out.println("Hooked findMatchingRecipe in " + classNode.name + " " + method.name);
                    }
                }
            }
        }
        return classNode;
    }
}
