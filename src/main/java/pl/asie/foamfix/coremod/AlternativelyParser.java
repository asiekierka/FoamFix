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

package pl.asie.foamfix.coremod;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AlternativelyParser extends ClassVisitor {
    public Map<String, String> fieldReplacements = new HashMap<>();
    public Map<String, String> methodReplacements = new HashMap<>();
    private final ClassNode node;

    public AlternativelyParser(int api, ClassNode node) {
        super(api);
        this.node = node;
    }

    public AlternativelyParser(int api, ClassNode node, ClassVisitor next) {
        super(api, next);
        this.node = node;
    }

    private boolean containsMethod(String name, String desc) {
        for (MethodNode methodNode : node.methods) {
            if (name.equals(methodNode.name) && desc.equals(methodNode.desc)) return true;
        }
        return false;
    }

    private boolean containsField(String name, String desc) {
        for (FieldNode fieldNode : node.fields) {
            if (name.equals(fieldNode.name) && desc.equals(fieldNode.desc)) return true;
        }
        return false;
    }

    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        if (!containsMethod(name, desc)) {
            return new Method(api, name, desc, super.visitMethod(access, name, desc, signature, exceptions));
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    public static class Annotation extends AnnotationVisitor {
        private final Map<String, String> targetMap;
        private final BiFunction<String, String, Boolean> verifier;
        private final String targetKey, targetDesc;

        public Annotation(int api, Map<String, String> map, BiFunction<String, String, Boolean> verifier, String key, String desc, AnnotationVisitor next) {
            super(api, next);
            this.verifier = verifier;
            this.targetMap = map;
            this.targetKey = key;
            this.targetDesc = desc;
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof String[] && "value".equals(name)) {
                for (String s : (String[]) value) {
                    if (verifier.apply(s, targetDesc)) {
                        targetMap.put(targetKey, s);
                        return;
                    }
                }
            }
        }
    }

    public class Method extends MethodVisitor {
        private final String name, desc;

        public Method(int api, String name, String desc, MethodVisitor mv) {
            super(api, mv);
            this.name = name;
            this.desc = desc;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            System.out.println(desc);
            return new Annotation(api, methodReplacements, AlternativelyParser.this::containsMethod, name, this.desc, super.visitAnnotation(desc, visible));
        }
    }

    public class Field extends FieldVisitor {
        private final String name, desc;

        public Field(int api, String name, String desc, FieldVisitor mv) {
            super(api, mv);
            this.name = name;
            this.desc = desc;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            System.out.println(desc);
            return new Annotation(api, fieldReplacements, AlternativelyParser.this::containsField, name, this.desc, super.visitAnnotation(desc, visible));
        }
    }
}
