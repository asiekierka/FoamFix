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

package pl.asie.foamfix.client;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedItemModel;
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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static pl.asie.foamfix.tests.BenchmarkBlockPos.a;

@SuppressWarnings("deprecation")
public class Deduplicator {
    public interface DeduplicatorFunction {
        Object deduplicate(Object o) throws Throwable;
    }

    private static final Set<Class> BLACKLIST_CLASS = Sets.newIdentityHashSet();
    private static final Set<Class> IMMUTABLE_CLASS = Sets.newIdentityHashSet();
    private static final Set<Class> TRIM_ARRAYS_CLASSES = Sets.newIdentityHashSet();

    private static final Style STYLE_EMPTY = new Style();

    // private static final MethodHandle EM_KEY_UNIVERSE_GETTER = MethodHandleHelper.findFieldGetter(EnumMap.class, "keyUniverse");
    // private static final MethodHandle EM_KEY_UNIVERSE_SETTER = MethodHandleHelper.findFieldSetter(EnumMap.class, "keyUniverse");

    private static final MethodHandle FIELD_UNPACKED_DATA_GETTER = MethodHandleHelper.findFieldGetter(UnpackedBakedQuad.class, "unpackedData");
    // private static final MethodHandle FIELD_UNPACKED_DATA_SETTER = MethodHandleHelper.findFieldSetter(UnpackedBakedQuad.class, "unpackedData");

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
    private final IDeduplicatingStorage<float[]> FLOATA_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.FLOAT_ARRAY);
    private final IDeduplicatingStorage<float[][]> FLOATAA_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.FLOAT_ARRAY_ARRAY);
    private final IDeduplicatingStorage<ItemCameraTransforms> ICT_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.ITEM_CAMERA_TRANSFORMS);
    private final IDeduplicatingStorage<Object> RESOURCE_LOCATION_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.GENERIC);
    private final IDeduplicatingStorage<Object> IMMUTABLE_COLLECTION_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.GENERIC);
    private final Set<Object> deduplicatedObjects = Sets.newIdentityHashSet();

    private final Map<Class, DeduplicatorFunction> DEDUPLICATOR_FUNCTIONS = new IdentityHashMap<>();
    private final Map<Class, DeduplicatorFunction> DEDUPLICATOR_0_FUNCTIONS = new IdentityHashMap<>();

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
        BLACKLIST_CLASS.add(String.class);
        BLACKLIST_CLASS.add(Integer.class);
        BLACKLIST_CLASS.add(Long.class);
        BLACKLIST_CLASS.add(Byte.class);
        BLACKLIST_CLASS.add(Boolean.class);
        BLACKLIST_CLASS.add(Float.class);
        BLACKLIST_CLASS.add(Double.class);
        BLACKLIST_CLASS.add(Short.class);
        BLACKLIST_CLASS.add(Class.class);
        BLACKLIST_CLASS.add(Field.class);
        BLACKLIST_CLASS.add(Method.class);
        BLACKLIST_CLASS.add(MethodHandle.class);
        BLACKLIST_CLASS.add(Vector3f.class);
        BLACKLIST_CLASS.add(Vector4f.class);
        BLACKLIST_CLASS.add(Matrix4f.class);
        BLACKLIST_CLASS.add(org.lwjgl.util.vector.Vector3f.class);
        BLACKLIST_CLASS.add(org.lwjgl.util.vector.Vector4f.class);
        BLACKLIST_CLASS.add(org.lwjgl.util.vector.Matrix4f.class);
        BLACKLIST_CLASS.add(Random.class);
        BLACKLIST_CLASS.add(TextureAtlasSprite.class);
        BLACKLIST_CLASS.add(ItemStack.class);
        BLACKLIST_CLASS.add(Gson.class);
        BLACKLIST_CLASS.add(GsonBuilder.class);
        BLACKLIST_CLASS.add(EnumBiMap.class);
        BLACKLIST_CLASS.add(ModelLoader.class);
        BLACKLIST_CLASS.add(Minecraft.class);
        BLACKLIST_CLASS.add(BlockModelShapes.class);
        BLACKLIST_CLASS.add(BlockFaceUV.class); // BlockPartFace handles it
        BLACKLIST_CLASS.add(ModelManager.class);
        BLACKLIST_CLASS.add(BlockPartRotation.class); // not handled
        BLACKLIST_CLASS.add(ModelBlockAnimation.class); // not handled
        BLACKLIST_CLASS.add(BufferBuilder.class);
        BLACKLIST_CLASS.add(SoundHandler.class);
        BLACKLIST_CLASS.add(SoundManager.class);
        BLACKLIST_CLASS.add(GameSettings.class);

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

    public void addResourceLocation(Object o) {
        RESOURCE_LOCATION_STORAGE.deduplicate(o);
    }

    public void addResourceLocation(Collection coll) {
        for (Object o : coll)
            RESOURCE_LOCATION_STORAGE.deduplicate(o);
    }

    public Deduplicator() {
        DEDUPLICATOR_0_FUNCTIONS.put(float[].class, (o) -> FLOATA_STORAGE.deduplicate((float[]) o));
        DeduplicatorFunction FLOATA_DEDUP = DEDUPLICATOR_0_FUNCTIONS.get(float[].class);

        DEDUPLICATOR_0_FUNCTIONS.put(float[][].class, (o) -> {
            float[][] arr = FLOATAA_STORAGE.deduplicate((float[][]) o);
            if (arr != o) {
                successfuls += arr.length;
            } else {
                for (int i = 0; i < arr.length; i++) {
                    float[] n = (float[]) FLOATA_DEDUP.deduplicate(arr[i]);
                    if (n != arr[i]) successfuls++;
                    arr[i] = n;
                }
            }
            return arr;
        });
        DeduplicatorFunction FLOATAA_DEDUP = DEDUPLICATOR_0_FUNCTIONS.get(float[][].class);

        DEDUPLICATOR_0_FUNCTIONS.put(float[][][].class, (o) -> {
            float[][][] arr = (float[][][]) o;
            for (int i = 0; i < arr.length; i++) {
                float[][] n = (float[][]) FLOATAA_DEDUP.deduplicate(arr[i]);
                if (n != arr[i]) successfuls++;
                arr[i] = n;
            }
            return arr;
        });
        DeduplicatorFunction FLOATAAA_DEDUP = DEDUPLICATOR_0_FUNCTIONS.get(float[][][].class);

        DEDUPLICATOR_0_FUNCTIONS.put(ResourceLocation.class, RESOURCE_LOCATION_STORAGE::deduplicate);
        for (Class c : Lists.newArrayList(ModelResourceLocation.class, Vec3d.class, Vec3i.class, BlockPos.class, TRSRTransformation.class)) {
            final IDeduplicatingStorage<Object> OBJECT_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.GENERIC);
            DEDUPLICATOR_0_FUNCTIONS.put(c, OBJECT_STORAGE::deduplicate);
        }

        if (FoamFixShared.isCoremod) {
            DEDUPLICATOR_0_FUNCTIONS.put(Style.class, obj -> deduplicateStyleIfCoremodPresent((Style) obj));
        } else {
            DEDUPLICATOR_0_FUNCTIONS.put(Style.class, obj -> obj);
        }

        DEDUPLICATOR_0_FUNCTIONS.put(ItemCameraTransforms.class, obj -> ICT_STORAGE.deduplicate((ItemCameraTransforms) obj));

        DEDUPLICATOR_FUNCTIONS.put(BlockPartFace.class, o -> {
            float[] n = (float[]) FLOATA_DEDUP.deduplicate(((BlockPartFace) o).blockFaceUV.uvs);
            if (n != ((BlockPartFace) o).blockFaceUV.uvs) {
                successfuls++;
            }
            return o;
        });

        if (FoamFixShared.config.expUnpackBakedQuads) {
            DEDUPLICATOR_FUNCTIONS.put(BakedQuad.class, o -> {
                BakedQuad quad = (BakedQuad) o;
                UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(quad.getFormat());
                quad.pipe(builder);
                o = builder.build();
                return o;
            });
        } else {
            DEDUPLICATOR_FUNCTIONS.put(BakedQuad.class, o -> o);
        }
        DEDUPLICATOR_FUNCTIONS.put(UnpackedBakedQuad.class, o -> {
            try {
                float[][][] array = (float[][][]) FIELD_UNPACKED_DATA_GETTER.invokeExact((UnpackedBakedQuad) o);
                FLOATAAA_DEDUP.deduplicate(array);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return o;
        });

        DEDUPLICATOR_FUNCTIONS.put(ItemOverrideList.class, obj -> {
            if (obj != ItemOverrideList.NONE) {
                List list = (List) IOL_OVERRIDES_GETTER.invokeExact((ItemOverrideList) obj);
                if (list.isEmpty()) {
                    successfuls++;
                    return ItemOverrideList.NONE;
                }
            }
            return obj;
        });

        DEDUPLICATOR_FUNCTIONS.put(AnimationItemOverrideList.class, obj -> {
            List list = (List) IOL_OVERRIDES_GETTER.invokeExact((ItemOverrideList) obj);
            if (list.isEmpty()) {
                successfuls++;
                IOL_OVERRIDES_SETTER.invokeExact((ItemOverrideList) obj, (List) ImmutableList.of());
            }
            return obj;
        });
    }

    private DeduplicatorFunction getDeduplicate0Func(Class c) {
        DeduplicatorFunction func = DEDUPLICATOR_0_FUNCTIONS.get(c);
        if (func == null) {
            if (ImmutableList.class.isAssignableFrom(c) || ImmutableMap.class.isAssignableFrom(c) || ImmutableSet.class.isAssignableFrom(c)) {
                func = IMMUTABLE_COLLECTION_STORAGE::deduplicate;
                DEDUPLICATOR_0_FUNCTIONS.put(c, func);
            }
        }
        return func;
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

    private static final Set<Class> cSet = new HashSet<>();
    private static final boolean cSetProp;

    static {
        String s = System.getProperty("pl.asie.foamfix.debugDeduplicatedClasses", "");
        cSetProp = !s.isEmpty();
    }

    public Object deduplicateObject(Object o, int recursion) {
        if (o == null || recursion > maxRecursion)
            return o;

        Class c = o.getClass();
        if (!shouldCheckClass(c))
            return o;

        if (!deduplicatedObjects.add(o))
            return o;

        if (cSetProp) {
            if (cSet.add(c)) {
                System.out.println("-" + Strings.repeat("-", recursion) + " " + c.getName());
            }
        }

        DeduplicatorFunction func = DEDUPLICATOR_FUNCTIONS.get(c);
        if (func == null) {
            boolean continueProcessing = true;

            if (o instanceof IBakedModel) {
                DeduplicatorFunction immMapFunc = getDeduplicate0Func(ImmutableMap.class);

                if (o instanceof PerspectiveMapWrapper) {
                    func = obj -> {
                        try {
                            Object to = IPAM_MW_TRANSFORMS_GETTER.invoke(obj);
                            Object toD = immMapFunc.deduplicate(to);
                            if (toD != null && to != toD) {
                                successfuls++;
                                IPAM_MW_TRANSFORMS_SETTER.invoke(obj, toD);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        return obj;
                    };
                } else if (c == BakedItemModel.class) {
                    func = obj -> {
                        try {
                            Object to = BIM_TRANSFORMS_GETTER.invoke(obj);
                            Object toD = immMapFunc.deduplicate(to);
                            if (toD != null && to != toD) {
                                successfuls++;
                                BIM_TRANSFORMS_SETTER.invoke(obj, toD);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        return obj;
                    };
                } else if (c == SimpleBakedModel.class) {
                    func = obj -> {
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            List l = ((IBakedModel) obj).getQuads(null, facing, 0);
                            trimArray(l);
                        }
                        return obj;
                    };
                }
            } else if (o instanceof BakedQuad) {
                BLACKLIST_CLASS.add(c);
                return o;
            } else if (o instanceof Item || o instanceof Block || o instanceof World
                    || o instanceof Entity || o instanceof Logger || o instanceof IRegistry
                    || o instanceof SimpleReloadableResourceManager || o instanceof IResourcePack
                    || o instanceof IRecipe || o instanceof Ingredient
                    || o instanceof LanguageManager || o instanceof BlockPos.MutableBlockPos) {
                BLACKLIST_CLASS.add(c);
                return o;
            } else if (o instanceof ItemOverrideList) {
                BLACKLIST_CLASS.add(c);
                return o;
            } else if (o instanceof java.util.Optional) {
                func = obj -> {
                    java.util.Optional opt = (java.util.Optional) obj;
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
                        } else {
                            return opt;
                        }
                    } else {
                        return opt;
                    }
                };
                continueProcessing = false;
            } else if (o instanceof com.google.common.base.Optional) {
                func = obj -> {
                    Optional opt = (Optional) obj;
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
                        } else {
                            return opt;
                        }
                    } else {
                        return opt;
                    }
                };
                continueProcessing = false;
            } else if (o instanceof Multimap) {
                if (o instanceof ImmutableMultimap || o instanceof SortedSetMultimap) {
                    func = obj -> {
                        for (Object value : ((Multimap) obj).values()) {
                            deduplicateObject(value, recursion + 1);
                        }
                        return obj;
                    };
                } else {
                    func = obj -> {
                        for (Object key : ((Multimap) obj).keySet()) {
                            List l = Lists.newArrayList(((Multimap) obj).values());
                            for (int i = 0; i < l.size(); i++) {
                                l.set(i, deduplicateObject(l.get(i), recursion + 1));
                            }

                            ((Multimap) obj).replaceValues(key, l);
                        }
                        return obj;
                    };
                }
                continueProcessing = false;
            } else if (o instanceof Map) {
                if (o instanceof SortedMap || IMMUTABLE_CLASS.contains(c)) {
                    func = obj -> {
                        for (Object v : ((Map) obj).keySet()) {
                            deduplicateObject(v, recursion + 1);
                        }

                        for (Object v : ((Map) obj).values()) {
                            deduplicateObject(v, recursion + 1);
                        }
                        return obj;
                    };
                } else if (o instanceof ImmutableBiMap) {
                    func = obj -> {
                        ImmutableMap im = (ImmutableMap) obj;
                        ImmutableMap.Builder newMap = ImmutableBiMap.builder();
                        boolean deduplicated = false;
                        for (Object key : im.keySet()) {
                            key = deduplicateObject(key, recursion + 1);
                            Object a = im.get(key);
                            Object b = deduplicateObject(a, recursion + 1);
                            newMap.put(key, b != null ? b : a);
                            if (b != null && b != a)
                                deduplicated = true;
                        }
                        return deduplicated ? newMap.build() : obj;
                    };
                } else if (o instanceof ImmutableMap) {
                    func = obj -> {
                        ImmutableMap im = (ImmutableMap) obj;
                        ImmutableMap.Builder newMap = ImmutableMap.builder();
                        boolean deduplicated = false;
                        for (Object key : im.keySet()) {
                            key = deduplicateObject(key, recursion + 1);
                            Object a = im.get(key);
                            Object b = deduplicateObject(a, recursion + 1);
                            newMap.put(key, b != null ? b : a);
                            if (b != null && b != a)
                                deduplicated = true;
                        }
                        return deduplicated ? newMap.build() : obj;
                    };
                } else {
                    func = obj -> {
                        try {
                            for (Object key : ((Map) obj).keySet()) {
                                key = deduplicateObject(key, recursion + 1);
                                Object value = ((Map) obj).get(key);
                                Object valueD = deduplicateObject(value, recursion + 1);
                                if (valueD != null && value != valueD)
                                    ((Map) obj).put(key, valueD);
                            }
                        } catch (UnsupportedOperationException e) {
                            IMMUTABLE_CLASS.add(c);
                            DEDUPLICATOR_FUNCTIONS.remove(c);
                            for (Object v : ((Map) obj).values()) {
                                deduplicateObject(v, recursion + 1);
                            }
                        }
                        return obj;
                    };
                }
                continueProcessing = false;
            } else if (o instanceof Collection) {
                if (o instanceof List) {
                    if (IMMUTABLE_CLASS.contains(c)) {
                        func = obj -> {
                            List l = (List) obj;
                            for (int i = 0; i < l.size(); i++) {
                                deduplicateObject(l.get(i), recursion + 1);
                            }
                            return obj;
                        };
                    } else if (o instanceof ImmutableList) {
                        func = obj -> {
                            ImmutableList il = (ImmutableList) obj;
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
                            } else {
                                return obj;
                            }
                        };
                    } else {
                        func = obj -> {
                            List l = (List) obj;
                            try {
                                for (int i = 0; i < l.size(); i++) {
                                    l.set(i, deduplicateObject(l.get(i), recursion + 1));
                                }
                            } catch (UnsupportedOperationException e) {
                                IMMUTABLE_CLASS.add(c);
                                DEDUPLICATOR_FUNCTIONS.remove(c);
                                for (int i = 0; i < l.size(); i++) {
                                    deduplicateObject(l.get(i), recursion + 1);
                                }
                            }
                            return obj;
                        };
                    }
                } else if (o instanceof ImmutableSet) {
                    if (!(o instanceof ImmutableSortedSet)) {
                        func = obj -> {
                            ImmutableSet.Builder builder = new ImmutableSet.Builder();
                            for (Object o1 : ((Set) obj)) {
                                builder.add(deduplicateObject(o1, recursion + 1));
                            }
                            return builder.build();
                        };
                    } else {
                        func = obj -> {
                            for (Object o1 : ((Set) obj)) {
                                deduplicateObject(o1, recursion + 1);
                            }
                            return obj;
                        };
                    }
                } else {
                    if (o instanceof Set && !(o instanceof SortedSet)) {
                        MethodHandle constructor;
                        try {
                            constructor = MethodHandles.publicLookup().findConstructor(c, MethodType.methodType(void.class));
                        } catch (Exception e) {
                            constructor = null;
                        }
                        if (constructor != null) {
                            try {
                                Collection nctest = (Collection) constructor.invoke();
                                if (nctest != null) {
                                    final MethodHandle constructorFinal = constructor;
                                    func = obj -> {
                                        Collection nc = (Collection) constructorFinal.invoke();
                                        for (Object o1 : ((Collection) obj)) {
                                            nc.add(deduplicateObject(o1, recursion + 1));
                                        }
                                        return nc;
                                    };
                                }
                            } catch (Throwable t) {
                                func = null;
                            }
                        }
                    }

                    // fallback
                    if (func == null) {
                        func = obj -> {
                            for (Object o1 : ((Collection) obj)) {
                                deduplicateObject(o1, recursion + 1);
                            }
                            return obj;
                        };
                    }
                }
                continueProcessing = false;
            } else if (c.isArray()) {
                func = obj -> {
                    for (int i = 0; i < Array.getLength(obj); i++) {
                        Object entry = Array.get(obj, i);
                        Object entryD = deduplicateObject(entry, recursion + 1);
                        if (entryD != null && entry != entryD)
                            Array.set(obj, i, entryD);
                    }
                    return obj;
                };
                continueProcessing = false;
            }

            if (func == null) {
                DeduplicatorFunction d0func = getDeduplicate0Func(c);
                if (d0func == null) {
                    func = obj -> obj;
                } else {
                    func = obj -> {
                        Object n = d0func.deduplicate(obj);
                        if (n != obj) {
                            successfuls++;
                        }
                        return n;
                    };
                    continueProcessing = false;
                }
            }

            if (continueProcessing) {
                boolean canTrim = o instanceof Predicate || TRIM_ARRAYS_CLASSES.contains(c);

                ImmutableSet.Builder<MethodHandle[]> fsBuilder = ImmutableSet.builder();
                {
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
                }
                ImmutableSet<MethodHandle[]> methodHandles = fsBuilder.build();

                if (!methodHandles.isEmpty()) {
                    final DeduplicatorFunction oldFunc = func;
                    if (canTrim) {
                        func = obj -> {
                            obj = oldFunc.deduplicate(obj);
                            for (MethodHandle[] mh : methodHandles) {
                                try {
                                    // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                                    Object value = mh[0].invoke(obj);
                                    Object valueD = deduplicateObject(value, recursion + 1);

                                    if (valueD != null) {
                                        trimArray(valueD);
                                        if (valueD != value) {
                                            mh[1].invoke(obj, valueD);
                                        }
                                    }
                                } catch (IllegalAccessException e) {

                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                            return obj;
                        };
                    } else {
                        func = obj -> {
                            obj = oldFunc.deduplicate(obj);
                            for (MethodHandle[] mh : methodHandles) {
                                try {
                                    // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                                    Object value = mh[0].invoke(obj);
                                    Object valueD = deduplicateObject(value, recursion + 1);

                                    if (valueD != null) {
                                        if (valueD != value) {
                                            mh[1].invoke(obj, valueD);
                                        }
                                    }
                                } catch (IllegalAccessException e) {

                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                            return obj;
                        };
                    }
                }
            }

            DEDUPLICATOR_FUNCTIONS.put(c, func);
        }

        try {
            Object n = func.deduplicate(o);
            return n;
        } catch (Throwable t) {
            return o;
        }
    }
}
