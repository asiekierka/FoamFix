package pl.asie.patchy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.*;
import java.util.function.BiFunction;

public class Patchy implements IClassTransformer {
    private final Map<Class<?>, TransformerHandler<?>> handlerMap;
    private final Map<String, Multimap<Class, BiFunction>> localTransformers;
    private final Multimap<Class, BiFunction> globalTransformers;
    private final List<String> activeTransformers;

    public Patchy() {
        this.handlerMap = new HashMap<>();
        this.localTransformers = new HashMap<>();
        this.globalTransformers = HashMultimap.create();
        this.activeTransformers = new ArrayList<>();
    }

    public void addTransformerId(String id) {
        activeTransformers.add(id);
    }

    public <T> void registerHandler(Class<T> cls, TransformerHandler<T> handler) {
        handlerMap.put(cls, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> TransformerHandler<T> getHandler(Class<T> cls) {
        return (TransformerHandler<T>) handlerMap.get(cls);
    }

    private byte[] transformWithMap(byte[] data, String name, Multimap<Class, BiFunction> map) {
        if (map != null) {
            for (Class c : map.keys()) {
                TransformerHandler handler = handlerMap.get(c);
                Object o = handler.begin(data);
                for (BiFunction func : map.get(c)) {
                    o = func.apply(o, name);
                }
                data = handler.end(o);
            }
        }
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        basicClass = transformWithMap(basicClass, transformedName, globalTransformers);
        basicClass = transformWithMap(basicClass, transformedName, localTransformers.get(transformedName));
        return basicClass;
    }

    protected <T> void registerGlobalTransformer(Class<T> type, BiFunction<T, String, T> function) {
        globalTransformers.put(type, function);
    }

    protected <T> void registerLocalTransformer(String s, Class<T> type, BiFunction<T, String, T> function) {
        if (!localTransformers.containsKey(s)) {
            localTransformers.put(s, HashMultimap.create());
        }
        localTransformers.get(s).put(type, function);
    }
}
