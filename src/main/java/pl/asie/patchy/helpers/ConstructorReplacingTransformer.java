package pl.asie.patchy.helpers;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pl.asie.patchy.TransformerFunction;

import java.util.Set;
import java.util.function.Function;

public class ConstructorReplacingTransformer implements TransformerFunction<ClassVisitor> {
    private final String from, to;
    private final Set<String> methods;

    public ConstructorReplacingTransformer(String from, String to, String... methods) {
        this.from = from.replace('.', '/');
        this.to = to.replace('.', '/');
        this.methods = ImmutableSet.copyOf(methods);
    }

    private class FFClassVisitor extends ClassVisitor {
        public FFClassVisitor(int api, ClassVisitor next) {
            super(api, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            if (methods.contains(name)) {
                return new ConstructorReplacingTransformer.FFMethodVisitor(api, cv.visitMethod(access, name, desc, signature, exceptions));
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
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == Opcodes.NEW && from.equals(type)) {
                System.out.println("Replaced NEW for " + from + " to " + to);
                super.visitTypeInsn(opcode, to);
            } else {
                super.visitTypeInsn(opcode, type);
            }
        }


        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESPECIAL && "<init>".equals(name) && from.equals(owner)) {
                System.out.println("Replaced INVOKESPECIAL for " + from + " to " + to);
                super.visitMethodInsn(opcode, to, name, desc, itf);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }
    @Override
    public ClassVisitor apply(ClassVisitor visitor) {
        return new FFClassVisitor(Opcodes.ASM5, visitor);
    }
}
