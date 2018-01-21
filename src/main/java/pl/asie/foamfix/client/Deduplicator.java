/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
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
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import com.google.gson.Gson;
import gnu.trove.set.hash.TCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.animation.AnimationItemOverrideList;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.DeduplicatingStorageTrove;
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

    private static final Style STYLE_EMPTY = new Style();

    private static final MethodHandle EM_KEY_UNIVERSE_GETTER = MethodHandleHelper.findFieldGetter(EnumMap.class, "keyUniverse");
    private static final MethodHandle EM_KEY_UNIVERSE_SETTER = MethodHandleHelper.findFieldSetter(EnumMap.class, "keyUniverse");

    private static final MethodHandle FIELD_UNPACKED_DATA_GETTER = MethodHandleHelper.findFieldGetter(UnpackedBakedQuad.class, "unpackedData");
    private static final MethodHandle FIELD_UNPACKED_DATA_SETTER = MethodHandleHelper.findFieldSetter(UnpackedBakedQuad.class, "unpackedData");

    private static final MethodHandle IPAM_MW_TRANSFORMS_GETTER = MethodHandleHelper.findFieldGetter(PerspectiveMapWrapper.class, "transforms");
    private static final MethodHandle IPAM_MW_TRANSFORMS_SETTER = MethodHandleHelper.findFieldSetter(PerspectiveMapWrapper.class, "transforms");
    private static final MethodHandle BIM_TRANSFORMS_GETTER = MethodHandleHelper.findFieldGetter("net.minecraftforge.client.model.BakedItemModel", "transforms");
    private static final MethodHandle BIM_TRANSFORMS_SETTER = MethodHandleHelper.findFieldSetter("net.minecraftforge.client.model.BakedItemModel", "transforms");
    private static final MethodHandle IOL_OVERRIDES_GETTER = MethodHandleHelper.findFieldGetter(ItemOverrideList.class, "overrides", "field_188023_b");
    private static final MethodHandle IOL_OVERRIDES_SETTER = MethodHandleHelper.findFieldSetter(ItemOverrideList.class, "overrides", "field_188023_b");

    // private static final Field FIELD_VERTEX_DATA = ReflectionHelper.findField(BakedQuad.class, "vertexData", "field_178215_a");

    public int successfulTrims = 0;
    public int successfuls = 0;
    public int maxRecursion = 0;

    private final Map<Object, java.util.Optional> JAVA_OPTIONALS = new IdentityHashMap<>();
    private final Map<Object, com.google.common.base.Optional> GUAVA_OPTIONALS = new IdentityHashMap<>();
    private final IDeduplicatingStorage<Object[]> KEY_UNIVERSE_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.OBJECT_ARRAY);
    private final IDeduplicatingStorage<float[]> FLOATA_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.FLOAT_ARRAY);
    private final IDeduplicatingStorage<float[][]> FLOATAA_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.FLOAT_ARRAY_ARRAY);
    private final IDeduplicatingStorage OBJECT_STORAGE = new DeduplicatingStorageTrove(HashingStrategies.GENERIC);
    private final IDeduplicatingStorage<ItemCameraTransforms> ICT_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.ITEM_CAMERA_TRANSFORMS);
    // private final IDeduplicatingStorage<ItemTransformVec3f> IT3_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.ITEM_TRANSFORM_VEC3F);
    private final Set<Object> deduplicatedObjects = new TCustomHashSet<>(HashingStrategies.IDENTITY);
    // public final TObjectIntMap<Class> dedupObjDataMap = new TObjectIntHashMap<>();

    public Deduplicator() {
    }

    private static void addClassFromName(Set<Class> set, String className) {
        try {
            set.add(Class.forName(className));
        } catch (ClassNotFoundException e) {

        }
    }

    static {
        TRIM_ARRAYS_CLASSES.add(TextComponentKeybind.class);
        TRIM_ARRAYS_CLASSES.add(TextComponentScore.class);
        TRIM_ARRAYS_CLASSES.add(TextComponentSelector.class);
        TRIM_ARRAYS_CLASSES.add(TextComponentString.class);
        TRIM_ARRAYS_CLASSES.add(TextComponentTranslation.class);

        TRIM_ARRAYS_CLASSES.add(VertexFormat.class);
        TRIM_ARRAYS_CLASSES.add(ModelBlock.class);
        TRIM_ARRAYS_CLASSES.add(ItemOverrideList.class);
        TRIM_ARRAYS_CLASSES.add(FoamyItemLayerModel.DynamicItemModel.class);
        TRIM_ARRAYS_CLASSES.add(SimpleBakedModel.class);
        TRIM_ARRAYS_CLASSES.add(WeightedBakedModel.class);
        TRIM_ARRAYS_CLASSES.add(Multipart.class);

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
        BLACKLIST_CLASS.add(EnumBiMap.class);
        BLACKLIST_CLASS.add(ModelLoader.class);
        BLACKLIST_CLASS.add(Minecraft.class);
        BLACKLIST_CLASS.add(BlockModelShapes.class);
        BLACKLIST_CLASS.add(BlockFaceUV.class); // BlockPartFace handles it
        BLACKLIST_CLASS.add(ModelManager.class);
        BLACKLIST_CLASS.add(BlockPartRotation.class); // not handled
        BLACKLIST_CLASS.add(ModelBlockAnimation.class); // not handled
        BLACKLIST_CLASS.add(BufferBuilder.class);

        BLACKLIST_CLASS.add(Logger.class);
        BLACKLIST_CLASS.add(Joiner.class);
        BLACKLIST_CLASS.add(Tessellator.class);
        BLACKLIST_CLASS.add(Cache.class);
        BLACKLIST_CLASS.add(LoadingCache.class);
        BLACKLIST_CLASS.add(VertexFormatElement.class);
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
            if (ResourceLocation.class == c || ModelResourceLocation.class == c ||
                    Vec3d.class == c || Vec3i.class == c || BlockPos.class == c) {
                size = 16; // can't be bothered to measure string size
                n = OBJECT_STORAGE.deduplicate(o);
            } else if (Style.class == c && FoamFixShared.isCoremod) {
                n = deduplicateStyleIfCoremodPresent((Style) o);
            } else if (TRSRTransformation.class == c) {
                size = 257; // size after full, x86_64
                n = OBJECT_STORAGE.deduplicate(o);
            } else if (ItemCameraTransforms.class == c) {
                size = 80; // minimum size
                n = ICT_STORAGE.deduplicate((ItemCameraTransforms) o);
            } else {
                throw new RuntimeException("Unsupported: " + c);
            }
        }

        if (n != o) {
            successfuls++;
            //dedupObjDataMap.adjustOrPutValue(o.getClass(), 1, 1);
            FoamFixShared.ramSaved += size;
        }
        return n;
    }

    private Style deduplicateStyleIfCoremodPresent(Style s) {
        while (s.bold == null && s.italic == null && s.obfuscated == null && s.strikethrough == null && s.underlined == null
                && s.clickEvent == null && s.hoverEvent == null && s.color == null && s.insertion == null) {
            s = s.parentStyle;
            if (s == null) {
                return STYLE_EMPTY;
            }
        }
        return s;
    }

    private boolean trimArray(Object o) {
        if (o instanceof ArrayList) {
            ((ArrayList) o).trimToSize();
            successfulTrims++;
            return true;
        } else {
            return false;
        }
    }

    public Object deduplicateObject(Object o, int recursion) {
        if (o == null || recursion > maxRecursion)
            return o;

        Class c = o.getClass();
        if (!shouldCheckClass(c))
            return o;

        if (!deduplicatedObjects.add(o))
            return o;

        boolean canTrim = o instanceof Predicate || TRIM_ARRAYS_CLASSES.contains(c);

        // System.out.println("-" + Strings.repeat("-", recursion) + " " + c.getName());

        if (canTrim) {
            if (c == SimpleBakedModel.class) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    List l = ((SimpleBakedModel) o).getQuads(null, facing, 0);
                    trimArray(l);
                }
            }
        }

        if (o instanceof IBakedModel) {
            if (o instanceof PerspectiveMapWrapper) {
                try {
                    Object to = IPAM_MW_TRANSFORMS_GETTER.invoke(o);
                    Object toD = deduplicate0(to);
                    if (toD != null && to != toD) {
                        IPAM_MW_TRANSFORMS_SETTER.invoke(o, toD);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if ("net.minecraftforge.client.model.BakedItemModel".equals(c.getName())) {
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

        if (c == BlockPartFace.class) {
            ((BlockPartFace) o).blockFaceUV.uvs = (float[]) deduplicate0(((BlockPartFace) o).blockFaceUV.uvs);
            return o;
        }

        if (o instanceof BakedQuad) {
            if (c == BakedQuad.class) {
                if (FoamFixShared.config.expUnpackBakedQuads) {
                    BakedQuad quad = (BakedQuad) o;
                    UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(quad.getFormat());
                    quad.pipe(builder);
                    o = builder.build();
                    c = UnpackedBakedQuad.class;
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
            }
        } else if (o instanceof ResourceLocation || o instanceof TRSRTransformation || o instanceof BlockFaceUV || c == Style.class) {
            return deduplicate0(o);
        } else if (c == ItemCameraTransforms.class || c == Vec3d.class || c == Vec3i.class || c == BlockPos.class) {
            return deduplicate0(o);
            /* if (d != o)
                return d;
            TODO: Add ItemTransformVec3f dedup, maybe
            return o; */
        } else if (o instanceof Item || o instanceof Block || o instanceof World
                || o instanceof Entity || o instanceof Logger || o instanceof IRegistry) {
            BLACKLIST_CLASS.add(c);
            return o;
        } else if (o instanceof ItemOverrideList && o != ItemOverrideList.NONE) {
            try {
                List list = (List) IOL_OVERRIDES_GETTER.invokeExact((ItemOverrideList) o);
                if (list.isEmpty()) {
                    if (c == ItemOverrideList.class) {
                        successfuls++;
                        return ItemOverrideList.NONE;
                    } else if (c == AnimationItemOverrideList.class) {
                        IOL_OVERRIDES_SETTER.invokeExact((ItemOverrideList) o, (List) ImmutableList.of());
                        successfuls++;
                    }
                }
            } catch (Throwable t) {

            }
        } else if (o instanceof java.util.Optional) {
            java.util.Optional opt = (java.util.Optional) o;
            if (opt.isPresent()) {
                Object b = deduplicateObject(opt.get(), recursion + 1);
                if (b != null) {
                    if (JAVA_OPTIONALS.containsKey(b)) {
                        successfuls++;
                        return JAVA_OPTIONALS.get(b);
                    }

                    if (b != opt.get()) {
                        java.util.Optional opt2 = java.util.Optional.of(b);
                        JAVA_OPTIONALS.put(b, opt2);
                        return opt2;
                    } else {
                        JAVA_OPTIONALS.put(opt.get(), opt);
                        return opt;
                    }
                }
            } else {
                return opt;
            }
        } else if (o instanceof com.google.common.base.Optional) {
            Optional opt = (Optional) o;
            if (opt.isPresent()) {
                Object b = deduplicateObject(opt.get(), recursion + 1);
                if (b != null) {
                    if (GUAVA_OPTIONALS.containsKey(b)) {
                        successfuls++;
                        return GUAVA_OPTIONALS.get(b);
                    }

                    if (b != opt.get()) {
                        com.google.common.base.Optional opt2 = com.google.common.base.Optional.of(b);
                        GUAVA_OPTIONALS.put(b, opt2);
                        return opt2;
                    } else {
                        GUAVA_OPTIONALS.put(opt.get(), opt);
                        return opt;
                    }
                }
            } else {
                return opt;
            }
        } else if (o instanceof Multimap) {
            if (o instanceof ImmutableMultimap || o instanceof SortedSetMultimap) {
                for (Object value : ((Multimap) o).values()) {
                    deduplicateObject(value, recursion + 1);
                }
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
            if (o instanceof SortedMap) {
                for (Object v : ((Map) o).values()) {
                    deduplicateObject(v, recursion + 1);
                }
            } else if (o instanceof ImmutableMap) {
                ImmutableMap im = (ImmutableMap) o;
                ImmutableMap.Builder newMap = (o instanceof ImmutableBiMap) ? ImmutableBiMap.builder() : ImmutableMap.builder();
                boolean deduplicated = false;
                for (Object key : im.keySet()) {
                    Object a = im.get(key);
                    Object b = deduplicateObject(a, recursion + 1);
                    newMap.put(key, b != null ? b : a);
                    if (b != null && b != a)
                        deduplicated = true;
                }
                return deduplicated ? newMap.build() : o;
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
                ImmutableList.Builder builder = ImmutableList.builder();
                boolean deduplicated = false;
                for (int i = 0; i < il.size(); i++) {
                    Object a = il.get(i);
                    Object b = deduplicateObject(a, recursion + 1);
                    builder.add(b != null ? b : a);
                    if (b != null && b != a)
                        deduplicated = true;
                }
                if (deduplicated) {
                    return builder.build();
                }
            } else {
                List l = (List) o;
                for (int i = 0; i < l.size(); i++) {
                    l.set(i, deduplicateObject(l.get(i), recursion + 1));
                }
            }
        } else if (o instanceof ImmutableSet) {
            if (!(o instanceof ImmutableSortedSet)) {
                ImmutableSet.Builder builder = new ImmutableSet.Builder();
                for (Object o1 : ((Set) o)) {
                    builder.add(deduplicateObject(o1, recursion + 1));
                }
                o = builder.build();
            } else {
                for (Object o1 : ((Set) o)) {
                    deduplicateObject(o1, recursion + 1);
                }
            }
        } else if (o instanceof Collection) {
            if (!(o instanceof SortedSet)) {
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
                        if (nc != null) {
                            for (Object o1 : ((Collection) o)) {
                                nc.add(deduplicateObject(o1, recursion + 1));
                            }
                            return nc;
                        }
                    } catch (Throwable t) {

                    }
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
                        if ((f.getModifiers() & Modifier.STATIC) != 0)
                            continue;

                        if (shouldCheckClass(f.getType())) {
                            try {
                                f.setAccessible(true);
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

                    if (canTrim) trimArray(valueD);

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
