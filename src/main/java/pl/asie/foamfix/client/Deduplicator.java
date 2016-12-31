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
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import gnu.trove.set.hash.TCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Deduplicator {
    private static final Set<Class> BLACKLIST_CLASS = new HashSet<>();
    private static final Set<Class> TRIM_ARRAYS_CLASSES = new HashSet<>();
    private static final Map<Class, Set<MethodHandle[]>> CLASS_FIELDS = new HashMap<>();

    private static final MethodHandle FIELD_UNPACKED_DATA_GETTER = MethodHandleHelper.findFieldGetter(UnpackedBakedQuad.class, "unpackedData");
    private static final MethodHandle FIELD_UNPACKED_DATA_SETTER = MethodHandleHelper.findFieldSetter(UnpackedBakedQuad.class, "unpackedData");
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
        TRIM_ARRAYS_CLASSES.add(ItemOverrideList.class);
        TRIM_ARRAYS_CLASSES.add(SimpleBakedModel.class);
        TRIM_ARRAYS_CLASSES.add(WeightedBakedModel.class);

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
        BLACKLIST_CLASS.add(BlockPart.class);
        BLACKLIST_CLASS.add(Minecraft.class);
        BLACKLIST_CLASS.add(BlockModelShapes.class);
        BLACKLIST_CLASS.add(ModelManager.class);

        BLACKLIST_CLASS.add(BakedQuad.class);

        // Intentionally smaller field sets in order to optimize
    }

    public void addObjectsToObjectStorage(Collection<Object> coll) {
        OBJECT_STORAGE.addAll(coll);
    }

    public Object deduplicate0(Object o) {
        Class c = o.getClass();
        Object n = o;
        int size = 0;

        if (float[].class.isAssignableFrom(c)) {
            size = 24 + ((float[]) o).length * 4;
            n = FLOATA_STORAGE.deduplicate((float[]) o);
        } else if (ResourceLocation.class == c) {
            size = 16; // can't be bothered to measure string size
            n = OBJECT_STORAGE.deduplicate(o);
        } else if (TRSRTransformation.class == c) {
            size = 257; // size after full, x86_64
            n = OBJECT_STORAGE.deduplicate(o);
        } else if (ItemCameraTransforms.class == c) {
            size = 80; // minimum size
            n = ICT_STORAGE.deduplicate((ItemCameraTransforms) o);
        } else if (float[][].class.isAssignableFrom(c)) {
            size = 16 + ((float[][]) o).length * 4; // assuming 32-bit pointers (worse case)
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

        if (n != o) {
            successfuls++;
            FoamFixShared.ramSaved += size;
        }
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

        if (c == UnpackedBakedQuad.class) {
            try {
                float[][][] array = (float[][][]) FIELD_UNPACKED_DATA_GETTER.invokeExact((UnpackedBakedQuad) o);
                // float[][][]s are not currently deduplicated
                deduplicate0(array);
            } catch (Throwable t) {
                t.printStackTrace();
            }
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
        } else if (c.isArray()) {
            if (!c.getComponentType().isPrimitive()) {
                for (int i = 0; i < Array.getLength(o); i++) {
                    Object entry = Array.get(o, i);
                    Object entryD = deduplicateObject(entry, recursion + 1);
                    if (entryD != null && entry != entryD)
                        Array.set(o, i, entryD);
                }
            } else {
                BLACKLIST_CLASS.add(c);
            }
        } else {
            if (!CLASS_FIELDS.containsKey(c)) {
                ImmutableSet.Builder<MethodHandle[]> fsBuilder = ImmutableSet.builder();
                Class cc = c;
                do {
                    for (Field f : cc.getDeclaredFields()) {
                        f.setAccessible(true);
                        if ((f.getModifiers() & Modifier.STATIC) != 0)
                            continue;

                        if (!f.getType().isPrimitive() && !f.getType().isEnum() && !BLACKLIST_CLASS.contains(f.getType())) {
                            try {
                                fsBuilder.add(new MethodHandle[]{
                                        MethodHandles.lookup().unreflectGetter(f),
                                        MethodHandles.lookup().unreflectSetter(f)
                                });
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } while ((cc = cc.getSuperclass()) != Object.class);
                CLASS_FIELDS.put(c, fsBuilder.build());
            }

            for (MethodHandle[] mh : CLASS_FIELDS.get(c)) {
                try {
                    // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                    Object value = mh[0].invoke(o);
                    Object valueD = deduplicateObject(value, recursion + 1);

                    if (TRIM_ARRAYS_CLASSES.contains(c)) {
                        if (valueD instanceof ArrayList) {
                            ((ArrayList) valueD).trimToSize();
                        }
                    }

                    if (valueD != null && value != valueD)
                        mh[1].invoke(o, valueD);
                } catch (IllegalAccessException e) {

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        return o;
    }
}
