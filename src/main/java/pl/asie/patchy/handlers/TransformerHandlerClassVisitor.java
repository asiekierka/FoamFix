package pl.asie.patchy.handlers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import pl.asie.patchy.Patchy;
import pl.asie.patchy.TransformerFunction;
import pl.asie.patchy.TransformerHandler;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class TransformerHandlerClassVisitor extends TransformerHandler<ClassVisitor> {
    public TransformerHandlerClassVisitor(Patchy owner) {
        super(owner);
    }

    @Override
    protected Class<ClassVisitor> getType() {
        return ClassVisitor.class;
    }

    @Override
    protected byte[] process(byte[] data, String name, List<TransformerFunction<ClassVisitor>> transformerFunctions) {
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = writer;
        for (int i = transformerFunctions.size() - 1; i >= 0; i--) {
            visitor = transformerFunctions.get(i).apply(visitor, name);
        }
        ClassReader reader = new ClassReader(data);
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }
}
