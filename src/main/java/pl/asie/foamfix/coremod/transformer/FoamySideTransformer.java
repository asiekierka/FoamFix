/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package pl.asie.foamfix.coremod.transformer;

import jdk.internal.org.objectweb.asm.Opcodes;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FoamySideTransformer implements IClassTransformer {
    public static final String SIDEONLY_DESCRIPTOR = Type.getDescriptor(SideOnly.class);

    public static class SideCapturingAnnotationVisitor extends AnnotationVisitor {
        public final Collection<String> targetSet;
        public final String targetName;

        public SideCapturingAnnotationVisitor(int api, Collection<String> targetSet, String targetName) {
            super(api);
            this.targetSet = targetSet;
            this.targetName = targetName;
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            if (name.equals("value") && !value.equals(SIDE)) {
                targetSet.add(targetName);
            }
        }
    }

    public static class SideCapturingClassVisitor extends ClassVisitor {
        public List<String> removableClasses = new ArrayList<>(1);
        public Set<String> removableFields = new HashSet<>();
        public Set<String> removableMethods = new HashSet<>();
        public String className;

        public SideCapturingClassVisitor(int api) {
            super(api);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.className = name;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(SIDEONLY_DESCRIPTOR)) {
                return new SideCapturingAnnotationVisitor(api, removableClasses, className);
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
            final String name1 = name+desc;
            return new FieldVisitor(api, super.visitField(access, name, desc, signature, value)) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(SIDEONLY_DESCRIPTOR)) {
                        return new SideCapturingAnnotationVisitor(api, removableFields, name1);
                    } else {
                        return null;
                    }
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            final String name1 = name+desc;
            return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(SIDEONLY_DESCRIPTOR)) {
                        return new SideCapturingAnnotationVisitor(api, removableMethods, name1);
                    } else {
                        return null;
                    }
                }
            };
        }
    }

    public static class SideRemovingClassVisitor extends ClassVisitor {
        private final SideCapturingClassVisitor capturer;

        public SideRemovingClassVisitor(int api, ClassVisitor cv, SideCapturingClassVisitor capturer) {
            super(api, cv);
            this.capturer = capturer;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
            final String name1 = name+desc;
            if (capturer.removableFields.contains(name1))
                return null;
            else
                return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            final String name1 = name+desc;
            if (capturer.removableMethods.contains(name1))
                return null;
            else
                return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static String SIDE = FMLLaunchHandler.side().name();
    private static final boolean DEBUG = false;

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if (bytes == null) { return null; }

        ClassReader classReader = new ClassReader(bytes);
        SideCapturingClassVisitor sideCapturingClassVisitor = new SideCapturingClassVisitor(Opcodes.ASM5);
        classReader.accept(sideCapturingClassVisitor, ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

        if (sideCapturingClassVisitor.removableClasses.size() > 0) {
            if (DEBUG)
            {
                System.out.println(String.format("Attempted to load class %s for invalid side %s", sideCapturingClassVisitor.className, SIDE));
            }
            throw new RuntimeException(String.format("Attempted to load class %s for invalid side %s", sideCapturingClassVisitor.className, SIDE));
        }

        if (sideCapturingClassVisitor.removableFields.size() > 0 || sideCapturingClassVisitor.removableMethods.size() > 0) {
            ClassWriter writer = new ClassWriter(0);
            classReader.accept(new SideRemovingClassVisitor(Opcodes.ASM5, writer, sideCapturingClassVisitor), 0);
            return writer.toByteArray();
        } else {
            return bytes;
        }
    }
}