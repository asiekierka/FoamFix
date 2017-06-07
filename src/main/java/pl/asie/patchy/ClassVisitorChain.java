package pl.asie.patchy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ClassVisitorChain {
    protected ChainableClassVisitor cv;
    private final ClassReader reader;

    public ClassVisitorChain(byte[] data) {
        this.reader = new ClassReader(data);
    }

    public ClassVisitorChain add(ChainableClassVisitor cv) {
        if (this.cv == null) {
            this.cv = cv;
        } else {
            this.cv.addClassVisitor(cv);
        }
        return this;
    }

    byte[] apply() {
        ClassWriter writer = new ClassWriter(0);
        this.cv.addClassVisitor(writer);
        reader.accept(cv, 0);
        return writer.toByteArray();
    }
}
