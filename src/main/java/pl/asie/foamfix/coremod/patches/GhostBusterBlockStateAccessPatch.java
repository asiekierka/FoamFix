/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.foamfix.coremod.patches;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pl.asie.patchy.TransformerFunction;

import java.util.Set;

public class GhostBusterBlockStateAccessPatch implements TransformerFunction<ClassVisitor> {
    public final Set<String> methods;

    public GhostBusterBlockStateAccessPatch(String... methods) {
        this.methods = ImmutableSet.copyOf(methods);
    }

    @Override
    public ClassVisitor apply(ClassVisitor visitor) {
        return new FFClassVisitor(Opcodes.ASM5, visitor);
    }

    private class FFClassVisitor extends ClassVisitor {
        private String className;

        public FFClassVisitor(int api, ClassVisitor next) {
            super(api, next);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            if (methods.contains(name)) {
                return new FFMethodVisitor(api, className, name, cv.visitMethod(access, name, desc, signature, exceptions));
            } else {
                return cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }
    }

    private static class FFMethodVisitor extends MethodVisitor {
        private final String className;
        private final String methodName;

        public FFMethodVisitor(int api, String className, String methodName, MethodVisitor next) {
            super(api, next);
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String desc, boolean itf) {
            // INVOKEVIRTUAL net/minecraft/world/World.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
            if (opcode == Opcodes.INVOKEVIRTUAL
                    && owner.startsWith("net/minecraft/world/")
                    && ("getBlockState".equals(name) || "func_180495_p".equals(name))
            ) {
                System.out.println("Added ghost buster patch (getBlockState call wrapped) in " + className + " " + methodName + " (" + owner + " " + name + " " + desc + ")");
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "pl/asie/foamfix/ghostbuster/GhostBusterSafeAccessors",
                        "getBlockState",
                        "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }
}
