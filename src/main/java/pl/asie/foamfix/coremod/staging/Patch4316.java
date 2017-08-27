package pl.asie.foamfix.coremod.staging;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;

// Note: Does not make EnumType.INT more accurate yet!
public class Patch4316 implements TransformerFunction<ClassNode> {
    private static final int[][] PATCH_FROM = new int[][] {
            new int[] {
                    Opcodes.I2F,
                    Opcodes.FDIV,
                    Opcodes.FCONST_2,
                    Opcodes.FMUL
            },
            new int[] {
                    Opcodes.I2F,
                    Opcodes.FMUL,
                    Opcodes.FCONST_2,
                    Opcodes.FDIV,
            },
    };

    private static final int[][] PATCH_TO = new int[][] {
            new int[] {
                    Opcodes.ICONST_1,
                    Opcodes.ISHR,
                    Opcodes.I2F,
                    Opcodes.FDIV
            },
            new int[] {
                    Opcodes.ICONST_1,
                    Opcodes.ISHR,
                    Opcodes.I2F,
                    Opcodes.FMUL
            },
    };

    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if ("unpack".equals(method.name) || "pack".equals(method.name)) {
                for (int idx = 0; idx < method.instructions.size(); idx++) {
                    for (int o = 0; o < PATCH_FROM.length; o++) {
                        ListIterator<AbstractInsnNode> oit = method.instructions.iterator(idx);
                        int i = 0;
                        for (; i < PATCH_FROM[o].length; i++) {
                            AbstractInsnNode onode = oit.next();
                            if (onode.getOpcode() != PATCH_FROM[o][i]) break;
                        }
                        if (i == PATCH_FROM[o].length) {
                            System.out.println("Patched inaccurate float handling! " + method.name);
                            for (; i < PATCH_FROM[o].length; i++) {
                                method.instructions.set(method.instructions.get(idx + i), new InsnNode(PATCH_TO[o][i]));
                            }
                        }
                    }
                }

                ListIterator<AbstractInsnNode> it = method.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node.getOpcode() == Opcodes.F2I) {
                        it.set(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "round", "(F)I", false));
                        System.out.println("Patched lack of rounding! " + method.name);
                    }
                }
            }
        }
        return classNode;
    }
}
