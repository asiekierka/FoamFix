package pl.asie.foamfix.coremod.patches;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import pl.asie.patchy.TransformerFunction;

import java.io.PrintWriter;
import java.util.ListIterator;

public class WorldServerRemovalPatch implements TransformerFunction<ClassNode> {
    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if ("updateEntities".equals(method.name) || "func_72939_s".equals(method.name)) {
                ListIterator<AbstractInsnNode> it = method.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node instanceof IntInsnNode && node.getOpcode() == Opcodes.SIPUSH
                            && ((IntInsnNode) node).operand == 300) {
                        AbstractInsnNode node2 = it.next();
                        if (node2.getOpcode() == Opcodes.IF_ICMPLT) {
                            it.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            it.add(new MethodInsnNode(
                                    Opcodes.INVOKEINTERFACE,
                                    "pl/asie/foamfix/coremod/patches/IFoamFixWorldRemovable",
                                    "foamfix_removeUnloadedEntities",
                                    "()V",
                                    true
                            ));
                            System.out.println("Patched updateEntities in " + classNode.name + " " + method.name);
                        }
                    }
                }
            }
        }

        return classNode;
    }
}
