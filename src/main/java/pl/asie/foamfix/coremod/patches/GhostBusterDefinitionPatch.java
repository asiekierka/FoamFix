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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import pl.asie.foamfix.ghostbuster.GhostBusterDefinition;
import pl.asie.foamfix.shared.FoamFixConfig;
import pl.asie.patchy.TransformerFunction;

import java.util.Set;

public class GhostBusterDefinitionPatch implements TransformerFunction<ClassNode> {
	private final GhostBusterDefinition definition;
	private final Set<String> methods;

	public GhostBusterDefinitionPatch(GhostBusterDefinition definition) {
		this.definition = definition;
		this.methods = ImmutableSet.of(this.definition.obfMethodName, this.definition.deobfMethodName);
	}

	@Override
	public ClassNode apply(ClassNode classNode) {
		for (MethodNode methodNode : classNode.methods) {
			if (methods.contains(methodNode.name)) {
				InsnList list = new InsnList();
				Label l = new Label();
				LabelNode ln = new LabelNode(l);
				list.add(new VarInsnNode(Opcodes.ALOAD, definition.accessPos));
				list.add(new VarInsnNode(Opcodes.ALOAD, definition.posPos));
				if (definition.radius == 0) {
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
							"pl/asie/foamfix/ghostbuster/GhostBusterSafeAccessors", "isBlockLoaded",
							"(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Z", false));
				} else {
					switch (definition.radius) {
						case 1:
							list.add(new InsnNode(Opcodes.ICONST_1));
							break;
						case 2:
							list.add(new InsnNode(Opcodes.ICONST_2));
							break;
						case 3:
							list.add(new InsnNode(Opcodes.ICONST_3));
							break;
						case 4:
							list.add(new InsnNode(Opcodes.ICONST_4));
							break;
						case 5:
							list.add(new InsnNode(Opcodes.ICONST_5));
							break;
						default:
							throw new RuntimeException("Invalid ghost buster radius: " + definition.radius);
					}
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
							"pl/asie/foamfix/ghostbuster/GhostBusterSafeAccessors", "isAreaLoaded",
							"(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;I)Z", false));
				}
				list.add(new JumpInsnNode(Opcodes.IFNE, ln));
				if (definition.returnValue != null) {
					if (definition.returnValue instanceof Boolean) {
						list.add(new InsnNode(((boolean) definition.returnValue) ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
						list.add(new InsnNode(Opcodes.IRETURN));
					} else {
						throw new RuntimeException("Invalid ghost buster return value: " + definition.returnValue);
					}
				} else {
					list.add(new InsnNode(Opcodes.RETURN));
				}
				list.add(ln);
				list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

				methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), list);
				System.out.println("Added ghost buster patch (radius = " + definition.radius + ") in " + classNode.name + " " + methodNode.name);
			}
		}
		return classNode;
	}
}
