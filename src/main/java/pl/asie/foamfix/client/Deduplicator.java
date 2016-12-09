/**
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
package pl.asie.foamfix.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import gnu.trove.set.hash.TCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.HashingStrategies;

import java.lang.reflect.Field;
import java.util.*;

public class Deduplicator {
    private static final Set<Class> BLACKLIST_CLASS = new HashSet<>();
    private static final Map<Class, Set<Field>> CLASS_FIELDS = new HashMap<>();

    private static final Field FIELD_UNPACKED_DATA = ReflectionHelper.findField(UnpackedBakedQuad.class, "unpackedData");
    private static final Field FIELD_VERTEX_DATA = ReflectionHelper.findField(BakedQuad.class, "vertexData", "field_178215_a");

    public int successfuls = 0;
    public int maxRecursion = 0;

    private DeduplicatingStorage<float[]> FLOATA_STORAGE = new DeduplicatingStorage<float[]>(HashingStrategies.FLOAT_ARRAY);
    private DeduplicatingStorage<float[][]> FLOATAA_STORAGE = new DeduplicatingStorage<float[][]>(HashingStrategies.FLOAT_ARRAY_ARRAY);
    private DeduplicatingStorage OBJECT_STORAGE = new DeduplicatingStorage(HashingStrategies.GENERIC);
    private DeduplicatingStorage<ItemCameraTransforms> ICT_STORAGE = new DeduplicatingStorage<>(HashingStrategies.ITEM_CAMERA_TRANSFORMS);
    private Set<Object> deduplicatedObjects = new TCustomHashSet<Object>(HashingStrategies.IDENTITY);

    public Deduplicator() {
    }

    static {
        BLACKLIST_CLASS.add(Class.class);
        BLACKLIST_CLASS.add(String.class);
        BLACKLIST_CLASS.add(Integer.class);
        BLACKLIST_CLASS.add(Long.class);
        BLACKLIST_CLASS.add(Byte.class);
        BLACKLIST_CLASS.add(Boolean.class);
        BLACKLIST_CLASS.add(Float.class);
        BLACKLIST_CLASS.add(Double.class);
        BLACKLIST_CLASS.add(Short.class);
        BLACKLIST_CLASS.add(TextureAtlasSprite.class);
        BLACKLIST_CLASS.add(ItemStack.class);
        BLACKLIST_CLASS.add(ModelRotation.class);
        BLACKLIST_CLASS.add(Gson.class);
        BLACKLIST_CLASS.add(ModelLoader.class);
        BLACKLIST_CLASS.add(Class.class);

        //BLACKLIST_CLASS.add(TRSRTransformation.class);
        BLACKLIST_CLASS.add(BlockPart.class);

        // Intentionally smaller field sets in order to optimize
    }

    public void addObjectsToObjectStorage(Collection<Object> coll) {
        OBJECT_STORAGE.addAll(coll);
    }

    public Object deduplicate0(Object o) {
        Class c = o.getClass();
        Object n = o;

        if (float[].class.isAssignableFrom(c)) {
            n = FLOATA_STORAGE.deduplicate((float[]) o);
        } else if (ResourceLocation.class == c || TRSRTransformation.class == c) {
            n = OBJECT_STORAGE.deduplicate(o);
        } else if (ItemCameraTransforms.class == c) {
            n = ICT_STORAGE.deduplicate((ItemCameraTransforms) o);
        } else if (float[][].class.isAssignableFrom(c)) {
            float[][] arr = FLOATAA_STORAGE.deduplicate((float[][]) o);
            if (arr != o) {
                n = arr;
                successfuls += arr.length;
            } else {
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = (float[]) deduplicate0(arr[i]);
                }
            }
        } else if (float[][][].class.isAssignableFrom(c)) {
            float[][][] arr = (float[][][]) o;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (float[][]) deduplicate0(arr[i]);
            }
        } else {
            return null;
        }

        if (n != o)
            successfuls++;
        return n;
    }

    public Object deduplicateObject(Object o, int recursion) {
        if (o == null || recursion > maxRecursion)
            return o;

        Class c = o.getClass();
        if (BLACKLIST_CLASS.contains(c) || deduplicatedObjects.contains(o))
            return o;
        else
            deduplicatedObjects.add(o);

        // System.out.println("-" + Strings.repeat("-", recursion) + " " + c.getName());

        if (o instanceof UnpackedBakedQuad && !FoamFixShared.enabledCoremodDeduplicator) {
            try {
                float[][][] array = (float[][][]) FIELD_UNPACKED_DATA.get(o);
                FIELD_UNPACKED_DATA.set(o, deduplicate0(array));
            } catch (IllegalAccessException e) {

            }
        } else if (o instanceof BakedQuad) {

        } else if (o instanceof ResourceLocation || o instanceof TRSRTransformation || (c == ItemCameraTransforms.class)) {
            return deduplicate0(o);
        } else if (o instanceof Item || o instanceof Block || o instanceof World
                || o instanceof Entity || o instanceof Logger
                || o instanceof IRegistry || c.isPrimitive() || c.isEnum()) {
            BLACKLIST_CLASS.add(c);
        } else if (o instanceof com.google.common.base.Optional) {
            Optional opt = (Optional) o;
            if (opt.isPresent()) {
                Object b = deduplicateObject(opt.get(), recursion + 1);
                if (b != null && b != opt.get()) {
                    return Optional.of(b);
                }
            }
        } else if (o instanceof ImmutableList) {
            ImmutableList il = (ImmutableList) o;
            List newList = new ArrayList();
            boolean deduplicated = false;
            for (int i = 0; i < il.size(); i++) {
                Object a = il.get(i);
                Object b = deduplicateObject(a, recursion + 1);
                newList.add(b != null ? b : a);
                if (b != null && b != a)
                    deduplicated = true;
            }
            if (deduplicated) {
                return ImmutableList.copyOf(newList);
            }
        } else if (o instanceof ImmutableMap) {
            ImmutableMap im = (ImmutableMap) o;
            Map newMap = new HashMap();
            boolean deduplicated = false;
            for (Object key : im.keySet()) {
                Object a = im.get(key);
                Object b = deduplicateObject(a, recursion + 1);
                newMap.put(key, b != null ? b : a);
                if (b != null && b != a)
                    deduplicated = true;
            }
            if (deduplicated) {
                return ImmutableMap.copyOf(newMap);
            }
        } else if (Map.class.isAssignableFrom(c)) {
            for (Object key : ((Map) o).keySet()) {
                Object value = ((Map) o).get(key);
                Object valueD = deduplicateObject(value, recursion + 1);
                if (valueD != null && value != valueD)
                    ((Map) o).put(key, valueD);
            }
        } else if (Collection.class.isAssignableFrom(c)) {
            Iterator i = ((Collection) o).iterator();
            while (i.hasNext()) {
                deduplicateObject(i.next(), recursion + 1);
            }
        } else {
            if (CLASS_FIELDS.containsKey(c)) {
                for (Field f : CLASS_FIELDS.get(c)) {
                    try {
                        // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                        Object value = f.get(o);
                        Object valueD = deduplicateObject(value, recursion + 1);
                        if (valueD != null && value != valueD)
                            f.set(o, valueD);
                    } catch (IllegalAccessException e) {

                    }
                }
            } else {
                HashSet<Field> fieldSet = new HashSet<>();
                Class cc = c;
                do {
                    for (Field f : cc.getDeclaredFields()) {
                        f.setAccessible(true);
                        fieldSet.add(f);
                        try {
                            // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                            Object value = f.get(o);
                            Object valueD = deduplicateObject(value, recursion + 1);
                            if (valueD != null && value != valueD)
                                f.set(o, valueD);
                        } catch (IllegalAccessException e) {

                        }
                    }
                } while ((cc = cc.getSuperclass()) != Object.class);
                CLASS_FIELDS.put(c, fieldSet);
            }
        }

        return o;
    }
}
