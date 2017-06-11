//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package pl.asie.foamfix.common;

import com.google.common.collect.Iterators;
import net.minecraft.util.ClassInheritanceMultiMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FoamyClassInheritanceMultiMap<T> extends ClassInheritanceMultiMap<T> {
    public FoamyClassInheritanceMultiMap(Class<T> p_i45909_1_) {
        super(p_i45909_1_);
    }

    public <S> Iterable<S> getByClass(final Class<S> p_getByClass_1_) {
        return new Iterable<S>() {
            public Iterator<S> iterator() {
                Class c = FoamyClassInheritanceMultiMap.this.initializeClassLookup(p_getByClass_1_);
                List<T> list = FoamyClassInheritanceMultiMap.this.map.get(c);
                if(list == null) {
                    return Collections.emptyIterator();
                } else {
                    Iterator<T> iterator = list.iterator();
                    return p_getByClass_1_ == c ? (Iterator<S>) iterator : Iterators.filter(iterator, p_getByClass_1_);
                }
            }
        };
    }
}
