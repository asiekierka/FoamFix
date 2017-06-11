package pl.asie.foamfix.coremod;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pl.asie.patchy.TransformerFunction;

import java.util.Set;

public class FoamFixReplaceClassSimpleName implements TransformerFunction<ClassVisitor> {
    public final Set<String> methods;

    public FoamFixReplaceClassSimpleName(String... methods) {
        this.methods = ImmutableSet.copyOf(methods);
    }

    @Override
    public ClassVisitor apply(ClassVisitor visitor) {
        return new FFClassVisitor(Opcodes.ASM5, visitor);
    }

    private class FFClassVisitor extends ClassVisitor {
        public FFClassVisitor(int api, ClassVisitor next) {
            super(api, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            if (methods.contains(name)) {
                return new FFMethodVisitor(api, cv.visitMethod(access, name, desc, signature, exceptions));
            } else {
                return cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }
    }

    private class FFMethodVisitor extends MethodVisitor {
        public FFMethodVisitor(int api, MethodVisitor next) {
            super(api, next);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String desc, boolean itf) {
            // INVOKEVIRTUAL java/lang/Class.getSimpleName ()Ljava/lang/String;
            if (opcode == Opcodes.INVOKEVIRTUAL && "getSimpleName".equals(name) && "java/lang/Class".equals(owner)) {
                System.out.println("Replaced INVOKEVIRTUAL getSimpleName");
                super.visitMethodInsn(opcode, owner, "getName", desc, itf);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }
}
