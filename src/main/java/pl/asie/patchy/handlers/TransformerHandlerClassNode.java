package pl.asie.patchy.handlers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import pl.asie.patchy.Patchy;
import pl.asie.patchy.TransformerFunction;
import pl.asie.patchy.TransformerHandler;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class TransformerHandlerClassNode extends TransformerHandler<ClassNode> {
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
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
