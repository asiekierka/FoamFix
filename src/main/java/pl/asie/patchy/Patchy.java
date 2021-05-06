/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
 */

package pl.asie.patchy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private byte[] transformWithMap(byte[] data, Map<Class, List<Object>> map) {
        if (map != null) {
            for (Class c : map.keySet()) {
                TransformerHandler handler = handlerMap.get(c);
                data = handler.process(data, map.get(c));
            }
        }
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        basicClass = transformWithMap(basicClass, globalTransformers);
        basicClass = transformWithMap(basicClass, localTransformers.row(transformedName));
        return basicClass;
    }

    <T> void registerGlobalTransformer(Class<T> type, TransformerFunction<T> function) {
        if (!globalTransformers.containsKey(type)) {
            globalTransformers.put(type, Lists.newArrayList(function));
        } else {
            globalTransformers.get(type).add(function);
        }
    }

    <T> void registerLocalTransformer(String s, Class<T> type, TransformerFunction<T> function) {
        if (!localTransformers.contains(s, type)) {
            localTransformers.put(s, type, Lists.newArrayList(function));
        } else {
            localTransformers.get(s, type).add(function);
        }
    }
}
