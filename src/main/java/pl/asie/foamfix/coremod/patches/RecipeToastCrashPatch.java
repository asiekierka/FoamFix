package pl.asie.foamfix.coremod.patches;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import pl.asie.patchy.TransformerFunction;
import pl.asie.patchy.handlers.TransformerHandlerClassNode;

import java.io.PrintWriter;
import java.util.ListIterator;

public class RecipeToastCrashPatch implements TransformerFunction<ClassNode> {
    @Override
    public ClassNode apply(ClassNode classNode) {
        int changes = 0;
        for (MethodNode method : classNode.methods) {
            if ("draw".equals(method.name) || "func_193653_a".equals(method.name)) {
                ListIterator<AbstractInsnNode> it = method.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node instanceof FieldInsnNode && node.getOpcode() == Opcodes.GETFIELD
                            && "net/minecraft/client/gui/toasts/RecipeToast".equals(((FieldInsnNode) node).owner)
                            && ("Ljava/util/List;".equals(((FieldInsnNode) node).desc))) {
                        AbstractInsnNode node2 = it.next();
                        if (node2 instanceof MethodInsnNode && node2.getOpcode() == Opcodes.INVOKEINTERFACE
                                && "java/util/List".equals(((MethodInsnNode) node2).owner)
                                && "size".equals(((MethodInsnNode) node2).name)) {
                            AbstractInsnNode node3 = it.next();
                            if (node3.getOpcode() == Opcodes.I2L) {
                                AbstractInsnNode node4 = it.next();
                                if (node4.getOpcode() == Opcodes.LDIV) {
                                    AbstractInsnNode node5 = it.next();
                                    if (node5.getOpcode() == Opcodes.LDIV) {
                                        it.previous();
                                        it.add(new InsnNode(Opcodes.DUP2));
                                        it.add(new InsnNode(Opcodes.LCONST_0));
                                        it.add(new InsnNode(Opcodes.LCMP));
                                        Label label = new Label();
                                        LabelNode labelNode = new LabelNode(label);
                                        it.add(new JumpInsnNode(Opcodes.IFNE, labelNode));
                                        it.add(new InsnNode(Opcodes.POP2));
                                        it.add(new InsnNode(Opcodes.LCONST_1));
                                        it.add(labelNode);
                                        classNode.access |= TransformerHandlerClassNode.RECOMPUTE_FRAMES;
                                        changes++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (changes > 1) {
            throw new RuntimeException("Too many changes in RecipeToastCrashPatch!");
        } else if (changes == 1) {
            System.out.println("Fixed RecipeToast crash in " + classNode.name);
        }

        return classNode;
    }
}
