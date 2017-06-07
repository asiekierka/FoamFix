package pl.asie.patchy.handlers;

import pl.asie.patchy.Patchy;
import pl.asie.patchy.TransformerFunction;
import pl.asie.patchy.TransformerHandler;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
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
    protected byte[] process(byte[] data, String name, List<TransformerFunction<byte[]>> transformerFunctions) {
        for (TransformerFunction<byte[]> function : transformerFunctions) {
            data = function.apply(data, name);
        }
        return data;
    }
}
