package pl.asie.patchy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class TransformerHandlerClassVisitorChain extends TransformerHandler<ClassVisitorChain> {
    public TransformerHandlerClassVisitorChain(Patchy owner) {
        super(owner);
    }

    @Override
    protected Class<ClassVisitorChain> getType() {
        return ClassVisitorChain.class;
    }

    @Override
    public ClassVisitorChain begin(byte[] data) {
        return new ClassVisitorChain(data);
    }

    @Override
    public byte[] end(ClassVisitorChain data) {
        return data.apply();
    }
}
