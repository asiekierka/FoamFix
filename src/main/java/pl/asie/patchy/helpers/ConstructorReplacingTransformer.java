/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
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
            if (methods.isEmpty() || methods.contains(name)) {
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
