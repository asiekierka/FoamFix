package pl.asie.patchy;

import java.util.function.BiFunction;

public interface TransformerFunction<T> extends BiFunction<T, String, T> {
}
