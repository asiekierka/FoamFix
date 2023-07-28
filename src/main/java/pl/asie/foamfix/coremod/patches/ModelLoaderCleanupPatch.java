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
package pl.asie.foamfix.coremod.patches;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.ghostbuster.GhostBusterDefinition;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;
import java.util.Set;

public class ModelLoaderCleanupPatch implements TransformerFunction<ClassNode> {
	private static final Set<String> methods = ImmutableSet.of("onResourceManagerReload", "func_110549_a");

	@Override
	public ClassNode apply(ClassNode classNode) {
		for (MethodNode methodNode : classNode.methods) {
			if (methods.contains(methodNode.name)) {
				InsnList list = null;
				int modelLoaderId;
				int added = 0;
				ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
				while (it.hasNext()) {
					AbstractInsnNode node = it.next();
					if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESPECIAL
						&& ((MethodInsnNode) node).owner.equals("net/minecraftforge/client/model/ModelLoader")
						&& ((MethodInsnNode) node).name.equals("<init>")) {
						node = it.next();
						if (node.getOpcode() == Opcodes.ASTORE && node instanceof VarInsnNode) {
							modelLoaderId = ((VarInsnNode) node).var;
							list = new InsnList();
							list.add(new VarInsnNode(Opcodes.ALOAD, modelLoaderId));
							list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
									"pl/asie/foamfix/client/ModelLoaderCleanup",
									"cleanup",
									"(Lnet/minecraftforge/client/model/ModelLoader;)V",
									false
									));
						}
					} else if (node.getOpcode() == Opcodes.RETURN && list != null) {
						methodNode.instructions.insertBefore(node, list);
						added++;
					}
				}
				System.out.println("Added ModelLoader cleanup patch (" + added + " occurences).");
			}
		}
		return classNode;
	}
}
