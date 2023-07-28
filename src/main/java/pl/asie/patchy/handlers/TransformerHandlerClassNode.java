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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import pl.asie.patchy.Patchy;
import pl.asie.patchy.TransformerFunction;
import pl.asie.patchy.TransformerHandler;

import java.util.List;

public class TransformerHandlerClassNode extends TransformerHandler<ClassNode> {
    public static final int RECOMPUTE_FRAMES = 0x400000;

    public TransformerHandlerClassNode(Patchy owner) {
        super(owner);
    }

    @Override
    protected Class<ClassNode> getType() {
        return ClassNode.class;
    }

    @Override
    protected byte[] process(byte[] data, List<TransformerFunction<ClassNode>> transformerFunctions) {
        ClassReader reader = new ClassReader(data);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        for (TransformerFunction<ClassNode> func : transformerFunctions)
            node = func.apply(node);
        ClassWriter writer = new ClassWriter((node.access & RECOMPUTE_FRAMES) != 0 ? ClassWriter.COMPUTE_FRAMES : 0);
        node.access &= ~RECOMPUTE_FRAMES;
        node.accept(writer);
        return writer.toByteArray();
    }
}
