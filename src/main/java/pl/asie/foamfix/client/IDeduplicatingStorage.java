package pl.asie.foamfix.client;

public interface IDeduplicatingStorage<T> {
	T deduplicate(T o);
}
