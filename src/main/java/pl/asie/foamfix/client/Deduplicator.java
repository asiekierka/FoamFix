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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import com.google.gson.Gson;
import gnu.trove.set.hash.TCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.HashingStrategies;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("deprecation")
public class Deduplicator {
    private static final Set<Class> BLACKLIST_CLASS = new TCustomHashSet<>(HashingStrategies.IDENTITY);
    private static final Set<Class> TRIM_ARRAYS_CLASSES = new TCustomHashSet<>(HashingStrategies.IDENTITY);
    private static final Map<Class, Set<MethodHandle[]>> CLASS_FIELDS = new IdentityHashMap<>();
    private static final Map<Class, MethodHandle> COLLECTION_CONSTRUCTORS = new IdentityHashMap<>();

    private static final MethodHandle FIELD_UNPACKED_DATA_GETTER = MethodHandleHelper.findFieldGetter(UnpackedBakedQuad.class, "unpackedData");
    private static final MethodHandle FIELD_UNPACKED_DATA_SETTER = MethodHandleHelper.findFieldSetter(UnpackedBakedQuad.class, "unpackedData");

    private static final MethodHandle IPAM_MW_TRANSFORMS_GETTER = MethodHandleHelper.findFieldGetter(IPerspectiveAwareModel.MapWrapper.class, "transforms");
    private static final MethodHandle IPAM_MW_TRANSFORMS_SETTER = MethodHandleHelper.findFieldSetter(IPerspectiveAwareModel.MapWrapper.class, "transforms");
    private static final MethodHandle BIM_TRANSFORMS_GETTER = MethodHandleHelper.findFieldGetter("net.minecraftforge.client.model.ItemLayerModel$BakedItemModel", "transforms");
    private static final MethodHandle BIM_TRANSFORMS_SETTER = MethodHandleHelper.findFieldSetter("net.minecraftforge.client.model.ItemLayerModel$BakedItemModel", "transforms");

    // private static final Field FIELD_VERTEX_DATA = ReflectionHelper.findField(BakedQuad.class, "vertexData", "field_178215_a");

    public int successfuls = 0;
    public int maxRecursion = 0;

    private final IDeduplicatingStorage<float[]> FLOATA_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.FLOAT_ARRAY);
    private final IDeduplicatingStorage<float[][]> FLOATAA_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.FLOAT_ARRAY_ARRAY);
    private final IDeduplicatingStorage OBJECT_STORAGE = new DeduplicatingStorageTrove(HashingStrategies.GENERIC);
    private final IDeduplicatingStorage<ItemCameraTransforms> ICT_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.ITEM_CAMERA_TRANSFORMS);
    // private final IDeduplicatingStorage<ItemTransformVec3f> IT3_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.ITEM_TRANSFORM_VEC3F);
    private final Set<Object> deduplicatedObjects = new TCustomHashSet<>(HashingStrategies.IDENTITY);

    public Deduplicator() {
    }

    static {
        TRIM_ARRAYS_CLASSES.add(FoamyItemLayerModel.DynamicItemModel.class);
        TRIM_ARRAYS_CLASSES.add(SimpleBakedModel.class);

        BLACKLIST_CLASS.add(FoamyItemLayerModel.Dynamic3DItemModel.class);
        BLACKLIST_CLASS.add(Object.class);
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
        BLACKLIST_CLASS.add(Gson.class);
        BLACKLIST_CLASS.add(ModelLoader.class);
        BLACKLIST_CLASS.add(Class.class);
        BLACKLIST_CLASS.add(BlockPart.class);
        BLACKLIST_CLASS.add(Minecraft.class);
        BLACKLIST_CLASS.add(BlockModelShapes.class);
        BLACKLIST_CLASS.add(ModelManager.class);

        BLACKLIST_CLASS.add(Logger.class);
        BLACKLIST_CLASS.add(Joiner.class);
        BLACKLIST_CLASS.add(Tessellator.class);
        BLACKLIST_CLASS.add(WorldRenderer.class);
        BLACKLIST_CLASS.add(Cache.class);
        BLACKLIST_CLASS.add(LoadingCache.class);
        BLACKLIST_CLASS.add(VertexFormatElement.class);

        BLACKLIST_CLASS.add(BakedQuad.class);

        // Intentionally smaller field sets in order to optimize
    }

    private boolean shouldCheckClass(Class c) {
        if (BLACKLIST_CLASS.contains(c))
            return false;

        if (c.isPrimitive() || c.isEnum() || (c.isArray() && !shouldCheckClass(c.getComponentType()))) {
            BLACKLIST_CLASS.add(c);
            return false;
        }

        return true;
    }

    public void addObject(Object o) {
        OBJECT_STORAGE.deduplicate(o);
    }

    public void addObjects(Collection coll) {
        for (Object o : coll)
            OBJECT_STORAGE.deduplicate(o);
    }

    public Object deduplicate0(Object o) {
        Object n = o;
        int size = 0;

        if (o instanceof float[]) {
            size = 24 + ((float[]) o).length * 4;
            n = FLOATA_STORAGE.deduplicate((float[]) o);
        } else if (o instanceof float[][]) {
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
        } else if (o instanceof float[][][]) {
            float[][][] arr = (float[][][]) o;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (float[][]) deduplicate0(arr[i]);
            }
        } else if (o instanceof ImmutableList || o instanceof ImmutableSet || o instanceof ImmutableMap) {
            n = OBJECT_STORAGE.deduplicate(o);
        } else {
            Class c = o.getClass();

            if (ResourceLocation.class == c || ModelResourceLocation.class == c) {
                size = 16; // can't be bothered to measure string size
                n = OBJECT_STORAGE.deduplicate(o);
            } else if (TRSRTransformation.class == c) {
                size = 257; // size after full, x86_64
                n = OBJECT_STORAGE.deduplicate(o);
            } else if (ItemCameraTransforms.class == c) {
                size = 80; // minimum size
                n = ICT_STORAGE.deduplicate((ItemCameraTransforms) o);
            } else {
                return null;
            }
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
        if (!shouldCheckClass(c))
            return o;

        if (!deduplicatedObjects.add(o))
            return o;

        // System.out.println("-" + Strings.repeat("-", recursion) + " " + c.getName());

        if (o instanceof IBakedModel) {
            if (o instanceof IPerspectiveAwareModel.MapWrapper) {
                try {
                    Object to = IPAM_MW_TRANSFORMS_GETTER.invoke(o);
                    Object toD = deduplicate0(to);
                    if (toD != null && to != toD) {
                        IPAM_MW_TRANSFORMS_SETTER.invoke(o, toD);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if ("net.minecraftforge.client.model.ItemLayerModel$BakedItemModel".equals(c.getName())) {
                try {
                    Object to = BIM_TRANSFORMS_GETTER.invoke(o);
                    Object toD = deduplicate0(to);
                    if (toD != null && to != toD) {
                        BIM_TRANSFORMS_SETTER.invoke(o, toD);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        if (c == UnpackedBakedQuad.class) {
            try {
                float[][][] array = (float[][][]) FIELD_UNPACKED_DATA_GETTER.invokeExact((UnpackedBakedQuad) o);
                // float[][][]s are not currently deduplicated
                deduplicate0(array);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (o instanceof ResourceLocation || o instanceof TRSRTransformation) {
            return deduplicate0(o);
        } else if (c == ItemCameraTransforms.class) {
            Object d = deduplicate0(o);
            if (d != o)
                return d;
            // TODO: Add ItemTransformVec3f dedup, maybe
            return o;
        } else if (o instanceof Item || o instanceof Block || o instanceof World
                || o instanceof Entity || o instanceof Logger || o instanceof IRegistry) {
            BLACKLIST_CLASS.add(c);
        } else if (o instanceof com.google.common.base.Optional) {
            Optional opt = (Optional) o;
            if (opt.isPresent()) {
                Object b = deduplicateObject(opt.get(), recursion + 1);
                if (b != null && b != opt.get()) {
                    return Optional.of(b);
                }
            }
        } else if (o instanceof Multimap) {
            if (o instanceof ImmutableMultimap) {
                // TODO: Handle me?
            } else {
                for (Object key : ((Multimap) o).keySet()) {
                    List l = Lists.newArrayList(((Multimap) o).values());
                    for (int i = 0; i < l.size(); i++) {
                        l.set(i, deduplicateObject(l.get(i), recursion + 1));
                    }

                    ((Multimap) o).replaceValues(key, l);
                }
            }
        } else if (o instanceof Map) {
            if (o instanceof ImmutableMap) {
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
            } else {
                for (Object key : ((Map) o).keySet()) {
                    Object value = ((Map) o).get(key);
                    Object valueD = deduplicateObject(value, recursion + 1);
                    if (valueD != null && value != valueD)
                        ((Map) o).put(key, valueD);
                }
            }
        } else if (o instanceof List) {
            if (o instanceof ImmutableList) {
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
            } else {
                List l = (List) o;
                for (int i = 0; i < l.size(); i++) {
                    l.set(i, deduplicateObject(l.get(i), recursion + 1));
                }
            }
        } else if (o instanceof Collection) {
            if (!COLLECTION_CONSTRUCTORS.containsKey(c)) {
                try {
                    COLLECTION_CONSTRUCTORS.put(c, MethodHandles.publicLookup().findConstructor(c, MethodType.methodType(void.class)));
                } catch (Exception e) {
                    COLLECTION_CONSTRUCTORS.put(c, null);
                }
            }

            MethodHandle constructor = COLLECTION_CONSTRUCTORS.get(c);
            if (constructor != null) {
                try {
                    Collection nc = (Collection) constructor.invoke();
                    for (Object o1 : ((Collection) o)) {
                        nc.add(deduplicateObject(o1, recursion + 1));
                    }
                    return nc;
                } catch (Throwable t) {

                }
            }

            // fallback
            for (Object o1 : ((Collection) o)) {
                deduplicateObject(o1, recursion + 1);
            }
        } else if (c.isArray()) {
            for (int i = 0; i < Array.getLength(o); i++) {
                Object entry = Array.get(o, i);
                Object entryD = deduplicateObject(entry, recursion + 1);
                if (entryD != null && entry != entryD)
                    Array.set(o, i, entryD);
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

                        if (shouldCheckClass(f.getType())) {
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
