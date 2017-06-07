package pl.asie.patchy;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class TransformerHandler<T> {
    private final Patchy owner;

    public TransformerHandler(Patchy owner) {
        this.owner = owner;
    }

    public void add(BiFunction<T, String, T> function, String... names) {
        if (names.length == 0) {
            owner.registerGlobalTransformer(getType(), function);
        } else {
            for (String s : names) {
                owner.registerLocalTransformer(s, getType(), function);
            }
        }
    }

    protected abstract Class<T> getType();
    protected abstract T begin(byte[] data);
    protected abstract byte[] end(T data);
}
