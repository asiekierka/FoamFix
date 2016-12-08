package pl.asie.foamfix.coremod;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.HashSet;

public class BlockPosPatch {
	private static final HashMap<String, String> mutableFieldSwaps = new HashMap<>();
	private static final HashSet<String> mutableDeletedMethods = new HashSet<>();

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
	}

	private static class BlockPosClassVisitor extends ClassVisitor {
		private final boolean isMutable;

		public BlockPosClassVisitor(int api, ClassVisitor next, boolean isMutable) {
			super(api, next);
			this.isMutable = isMutable;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc,
		                               String signature, Object value) {
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
				return new BlockPosMethodVisitor(api, cv.visitMethod(access, name, desc, signature, exceptions));
			} else {
				return null;
			}
		}
	}

	private static class BlockPosMethodVisitor extends MethodVisitor {
		public BlockPosMethodVisitor(int api, MethodVisitor mv) {
			super(api, mv);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
		                           String desc) {
			if ("net/minecraft/util/math/BlockPos$MutableBlockPos".equals(owner)) {
				String dst = mutableFieldSwaps.get(name);
				if (dst != null) {
					mv.visitFieldInsn(opcode, "net/minecraft/util/math/Vec3i", dst, desc);
				} else {
					mv.visitFieldInsn(opcode, owner, name, desc);
				}
			} else {
				mv.visitFieldInsn(opcode, owner, name, desc);
			}
		}
	}

	public static byte[] patchVec3i(byte[] data) {
		final ClassReader reader = new ClassReader(data);
		final ClassNode node = new ClassNode();
		reader.accept(node, 8);
		for (FieldNode fn : node.fields) {
			if ("I".equals(fn.desc) && fn.access == (Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL)) {
				fn.access = Opcodes.ACC_PROTECTED;
			}
		}
		final ClassWriter writer = new ClassWriter(8);
		node.accept(writer);
		return writer.toByteArray();
	}

	public static byte[] patchOtherClass(byte[] data, boolean isMutable) {
		final ClassReader reader = new ClassReader(data);
		final ClassWriter writer = new ClassWriter(8);
		reader.accept(new BlockPosClassVisitor(Opcodes.ASM5, writer, isMutable), 8);
		return writer.toByteArray();
	}
}
