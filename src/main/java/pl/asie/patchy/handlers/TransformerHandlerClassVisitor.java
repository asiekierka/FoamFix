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
