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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.HashSet;

public class BlockPosPatch {
	private static final HashMap<String, String> mutableFieldSwaps = new HashMap<>();
	private static final HashSet<String> mutableDeletedMethods = new HashSet<>();
	private static final HashSet<String> mutableOwners = new HashSet<>();

	static {
		mutableFieldSwaps.put("x", "x");
		mutableFieldSwaps.put("y", "y");
		mutableFieldSwaps.put("z", "z");

		mutableFieldSwaps.put("field_177997_b", "field_177962_a");
		mutableFieldSwaps.put("field_177998_c", "field_177960_b");
		mutableFieldSwaps.put("field_177996_d", "field_177961_c");

		mutableDeletedMethods.add("getX");
		mutableDeletedMethods.add("getY");
		mutableDeletedMethods.add("getZ");

		mutableDeletedMethods.add("func_177958_n");
		mutableDeletedMethods.add("func_177956_o");
		mutableDeletedMethods.add("func_177952_p");

		mutableOwners.add("net/minecraft/util/math/BlockPos$MutableBlockPos");
	}

	private static class BlockPosClassVisitor extends ClassVisitor {
		private boolean isMutable;
		private boolean hasChanged = false;

		public BlockPosClassVisitor(int api, ClassVisitor next) {
			super(api, next);
		}

		public void setCV(ClassVisitor visitor) {
			this.cv = visitor;
		}

		@Override
		public void visit(int version, int access, String name, String signature,
		                  String superName, String[] interfaces) {
			if ("net/minecraft/util/math/BlockPos$MutableBlockPos".equals(name)) {
				isMutable = true;
			}
			if (mutableOwners.contains(superName)) {
				mutableOwners.add(name);
			}
			if (cv != null) {
				cv.visit(version, access, name, signature, superName, interfaces);
			}
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc,
		                               String signature, Object value) {
			if (cv == null) {
				return null;
			}
			if (!isMutable || !mutableFieldSwaps.containsKey(name)) {
				return cv.visitField(access, name, desc, signature, value);
			} else {
				return null;
			}
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
		                                 String signature, String[] exceptions) {
			if (!isMutable || !mutableDeletedMethods.contains(name)) {
				return new BlockPosMethodVisitor(api, this, cv != null ? cv.visitMethod(access, name, desc, signature, exceptions) : null);
			} else {
				return null;
			}
		}
	}

	private static class BlockPosMethodVisitor extends MethodVisitor {
		private final BlockPosClassVisitor classVisitor;

		public BlockPosMethodVisitor(int api, BlockPosClassVisitor cv, MethodVisitor mv) {
			super(api, mv);
			classVisitor = cv;
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
		                           String desc) {
			if (mutableOwners.contains(owner)) {
				String dst = mutableFieldSwaps.get(name);
				if (dst != null) {
					if (mv != null) {
						mv.visitFieldInsn(opcode, "net/minecraft/util/math/Vec3i", dst, desc);
					}
					classVisitor.hasChanged = true;
				} else {
					if (mv != null) {
						mv.visitFieldInsn(opcode, owner, name, desc);
					}
				}
			} else {
				if (mv != null) {
					mv.visitFieldInsn(opcode, owner, name, desc);
				}
			}
		}
	}

	public static ClassNode patchVec3i(ClassNode node) {
		for (FieldNode fn : node.fields) {
			if ("I".equals(fn.desc) && fn.access == (Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL)) {
				fn.access = Opcodes.ACC_PROTECTED;
			}
		}
		return node;
	}

	public static ClassVisitor patchOtherClass(ClassVisitor next) {
		return new BlockPosClassVisitor(Opcodes.ASM5, next);
	}
}
