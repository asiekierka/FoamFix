/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.patchy.handlers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import pl.asie.patchy.Patchy;
import pl.asie.patchy.TransformerFunction;
import pl.asie.patchy.TransformerHandler;

import java.util.List;

public class TransformerHandlerClassVisitor extends TransformerHandler<ClassVisitor> {
    public TransformerHandlerClassVisitor(Patchy owner) {
        super(owner);
    }

    @Override
    protected Class<ClassVisitor> getType() {
        return ClassVisitor.class;
    }

    @Override
    protected byte[] process(byte[] data, List<TransformerFunction<ClassVisitor>> transformerFunctions) {
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = writer;
        for (int i = transformerFunctions.size() - 1; i >= 0; i--) {
            visitor = transformerFunctions.get(i).apply(visitor);
        }
        ClassReader reader = new ClassReader(data);
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }
}
