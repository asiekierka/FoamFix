package pl.asie.patchy;

import com.google.common.collect.*;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.*;
import java.util.function.BiFunction;

public class Patchy implements IClassTransformer {
    private final Map<Class, TransformerHandler> handlerMap;
    private final Table<String, Class, List<Object>> localTransformers;
    private final Map<Class, List<Object>> globalTransformers;
    private final List<String> activeTransformers;

    public Patchy() {
        this.handlerMap = new HashMap<>();
        this.localTransformers = HashBasedTable.create();
        this.globalTransformers = new HashMap<>();
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

    private byte[] transformWithMap(byte[] data, String name, Map<Class, List<Object>> map) {
        if (map != null) {
            for (Class c : map.keySet()) {
                TransformerHandler handler = handlerMap.get(c);
                data = handler.process(data, name, map.get(c));
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
        basicClass = transformWithMap(basicClass, transformedName, localTransformers.row(transformedName));
        return basicClass;
    }

    protected <T> void registerGlobalTransformer(Class<T> type, TransformerFunction<T> function) {
        if (!globalTransformers.containsKey(type)) {
            globalTransformers.put(type, Lists.newArrayList(function));
        } else {
            globalTransformers.get(type).add(function);
        }
    }

    protected <T> void registerLocalTransformer(String s, Class<T> type, TransformerFunction<T> function) {
        if (!localTransformers.contains(s, type)) {
            localTransformers.put(s, type, Lists.newArrayList(function));
        } else {
            localTransformers.get(s, type).add(function);
        }
    }
}
