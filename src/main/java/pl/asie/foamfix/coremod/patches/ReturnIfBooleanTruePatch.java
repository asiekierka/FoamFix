/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
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
package pl.asie.foamfix.coremod.patches;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.patchy.TransformerFunction;

import java.util.Set;

public class ReturnIfBooleanTruePatch implements TransformerFunction<ClassNode> {
	private final String optionName;
	private final Set<String> methods;
	private final InsnList list;

	public ReturnIfBooleanTruePatch(String optionName, String... methods) {
		this.optionName = optionName;
		this.methods = ImmutableSet.copyOf(methods);

		list = new InsnList();
		Label l = new Label();
		LabelNode ln = new LabelNode(l);
		list.add(new FieldInsnNode(Opcodes.GETSTATIC, "pl/asie/foamfix/shared/FoamFixShared", "config", "Lpl/asie/foamfix/shared/FoamFixConfig;"));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/foamfix/shared/FoamFixConfig", optionName, "Z"));
		list.add(new JumpInsnNode(Opcodes.IFEQ, ln));
		list.add(new InsnNode(Opcodes.RETURN));
		list.add(ln);
		list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
	}

	@Override
	public ClassNode apply(ClassNode classNode) {
		for (MethodNode methodNode : classNode.methods) {
			if (methods.contains(methodNode.name)) {
				methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), list);
				System.out.println("Added return if option true in " + classNode.name + " " + methodNode.name);
			}
		}
		return classNode;
	}
}
