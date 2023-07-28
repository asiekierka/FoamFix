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
package pl.asie.foamfix.coremod.patches.network;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;

public class DeflaterCompressionLevelPatch implements TransformerFunction<ClassNode> {
	@Override
	public ClassNode apply(ClassNode classNode) {
		for (MethodNode method : classNode.methods) {
			if ("<init>".equals(method.name)) {
				ListIterator<AbstractInsnNode> it = method.instructions.iterator();
				while (it.hasNext()) {
					AbstractInsnNode node = it.next();
					if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESPECIAL
							&& ((MethodInsnNode) node).owner.equals("java/util/zip/Deflater")
							&& ((MethodInsnNode) node).name.equals("<init>")
							&& ((MethodInsnNode) node).desc.equals("()V")) {
						((MethodInsnNode) node).desc = "(I)V";
						it.previous();
						it.previous();
						it.add(new FieldInsnNode(
								Opcodes.GETSTATIC,
								"pl/asie/foamfix/shared/FoamFixShared",
								"neDeflaterCompression",
								"I"
						));
						System.out.println("Patched deflater init in " + classNode.name + " " + method.name);
					}
				}
			}
		}

		return classNode;
	}
}
