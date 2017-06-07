package pl.asie.patchy;

import java.util.function.Function;

public class TransformerHandlerByteArray extends TransformerHandler<byte[]> {
    public TransformerHandlerByteArray(Patchy owner) {
        super(owner);
    }

    @Override
    protected Class<byte[]> getType() {
        return byte[].class;
    }

    @Override
    public byte[] begin(byte[] data) {
        return data;
    }

    @Override
    public byte[] end(byte[] data) {
        return data;
    }
}
