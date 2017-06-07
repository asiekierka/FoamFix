package pl.asie.patchy;

import org.objectweb.asm.ClassVisitor;

public class ChainableClassVisitor extends ClassVisitor {
    public ChainableClassVisitor(int api) {
        super(api);
    }

    void addClassVisitor(ClassVisitor cv) {
        if (this.cv == null) {
            this.cv = cv;
        } else if (this.cv instanceof ChainableClassVisitor) {
            ((ChainableClassVisitor) this.cv).addClassVisitor(cv);
        } else {
            throw new RuntimeException("Broken ClassVisitor chain!");
        }
    }
}
