package pl.asie.patchy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class TransformerHandlerClassNode extends TransformerHandler<ClassNode> {
    public TransformerHandlerClassNode(Patchy owner) {
        super(owner);
    }

    @Override
    protected Class<ClassNode> getType() {
        return ClassNode.class;
    }

    @Override
    public ClassNode begin(byte[] data) {
        ClassReader reader = new ClassReader(data);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }

    @Override
    public byte[] end(ClassNode data) {
        ClassWriter writer = new ClassWriter(0);
        data.accept(writer);
        return writer.toByteArray();
    }
}
