package pl.asie.foamfix.coremod.patches;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.Opcodes;
import pl.asie.patchy.TransformerFunction;

import java.util.ListIterator;

/* Patch discovered by Aikar of PaperMC */
public class FastAirLookupPatch implements TransformerFunction<ClassNode> {
    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode node = it.next();
                if (node instanceof FieldInsnNode && node.getOpcode() == Opcodes.GETSTATIC
                        && "net/minecraft/init/Blocks".equals(((FieldInsnNode) node).owner)
                        && ("AIR".equals(((FieldInsnNode) node).name)
                        || "field_190931_a".equals(((FieldInsnNode) node).name))) {
                    AbstractInsnNode node2 = it.next();
                    if (node2 instanceof MethodInsnNode && node2.getOpcode() == Opcodes.INVOKESTATIC
                            && "net/minecraft/item/Item".equals(((MethodInsnNode) node2).owner)
                            && ("getItemFromBlock".equals(((MethodInsnNode) node2).name)
                            || "func_150898_a".equals(((MethodInsnNode) node2).name))) {
                        it.remove();
                        it.previous();
                        it.set(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "pl/asie/foamfix/FoamFix",
                                "getItemAir",
                                "()Lnet/minecraft/item/Item;",
                                false
                        ));
                        System.out.println("Replaced Item.getItemFromBlock(Blocks.AIR) in " + classNode.name + " " + method.name);
                    }
                }
            }
        }
        return classNode;
    }
}
