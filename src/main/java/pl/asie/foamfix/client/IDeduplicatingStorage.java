package pl.asie.foamfix.client;

/**
 * Created by asie on 1/7/17.
 */
public interface IDeduplicatingStorage<T> {
	T deduplicate(T o);
}
