package pl.asie.foamfix.coremod.patches;

import net.minecraft.network.datasync.EntityDataManager;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;
import pl.asie.foamfix.common.FoamyArrayBackedDataManagerMap;
import pl.asie.patchy.TransformerFunction;

import java.io.PrintWriter;
import java.util.ListIterator;
import java.util.Map;

public class EntityDataManagerPatch implements TransformerFunction<ClassNode> {
    public static Map<Integer, EntityDataManager.DataEntry<?>> newArrayBackedMap() {
        try {
            Class.forName("it.unimi.dsi.fastutil.ints.IntOpenHashSet");
            return new FoamyArrayBackedDataManagerMap.OneTwelve<>();
        } catch (ClassNotFoundException e) {
            return new FoamyArrayBackedDataManagerMap.OneEleven<>();
        }
    }

    @Override
    public ClassNode apply(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode node = it.next();
                if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESTATIC
                        && "com/google/common/collect/Maps".equals(((MethodInsnNode) node).owner)
                        && "newHashMap".equals(((MethodInsnNode) node).name)) {
                    AbstractInsnNode node2 = it.next();
                    if (node2 instanceof FieldInsnNode && node2.getOpcode() == Opcodes.PUTFIELD
                            && "net/minecraft/network/datasync/EntityDataManager".equals(((FieldInsnNode) node2).owner)
                            && ("entries".equals(((FieldInsnNode) node2).name)
                            || "field_187234_c".equals(((FieldInsnNode) node2).name))) {
                        while (!(it.previous() instanceof MethodInsnNode));
                        it.next();
                        it.set(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "pl/asie/foamfix/coremod/patches/EntityDataManagerPatch",
                                "newArrayBackedMap",
                                "()Ljava/util/Map;",
                                false
                        ));
                        it.next();
                        System.out.println("Replaced Maps.newHashMap() in " + classNode.name + " " + method.name);
                    }
                }
            }
        }
        return classNode;
    }
}
