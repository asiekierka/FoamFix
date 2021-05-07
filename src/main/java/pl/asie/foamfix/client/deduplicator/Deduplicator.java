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

package pl.asie.foamfix.client.deduplicator;

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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.client.FoamyItemLayerModel;
import pl.asie.foamfix.client.FoamyMultipartBakedModel;
import pl.asie.foamfix.client.IDeduplicatingStorage;
import pl.asie.foamfix.client.condition.FoamyConditionAnd;
import pl.asie.foamfix.client.condition.FoamyConditionOr;
import pl.asie.foamfix.client.condition.FoamyConditionPropertyValue;
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
import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("deprecation")
public class Deduplicator {

    private static final Map<Class, Boolean> SHOULD_PROCESS_CLASS = new IdentityHashMap<>();
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
    private final Map<Class, Deduplicator0Function> DEDUPLICATOR_0_FUNCTIONS = new IdentityHashMap<>();

    private static void addClassFromName(Set<Class> set, String className) {
        try {
            set.add(Class.forName(className));
        } catch (ClassNotFoundException e) {

        }
    }

    private static <T> void addClassFromName(Map<Class, T> map, String className, T value) {
        try {
            map.put(Class.forName(className), value);
        } catch (ClassNotFoundException e) {

        }
    }
    
    private static void forbidClass(Class c) {
        SHOULD_PROCESS_CLASS.put(c, false);
    }

    private static void permitClass(Class c) {
        SHOULD_PROCESS_CLASS.put(c, true);
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

        forbidClass(Object.class);
        forbidClass(String.class);
        forbidClass(Integer.class);
        forbidClass(Long.class);
        forbidClass(Byte.class);
        forbidClass(Boolean.class);
        forbidClass(Float.class);
        forbidClass(Double.class);
        forbidClass(Short.class);
        forbidClass(Class.class);
        forbidClass(Field.class);
        forbidClass(Method.class);
        forbidClass(MethodHandle.class);
        forbidClass(Vector3f.class);
        forbidClass(Vector4f.class);
        forbidClass(Matrix4f.class);
        forbidClass(org.lwjgl.util.vector.Vector3f.class);
        forbidClass(org.lwjgl.util.vector.Vector4f.class);
        forbidClass(org.lwjgl.util.vector.Matrix4f.class);
        forbidClass(Random.class);
        forbidClass(TextureAtlasSprite.class);
        forbidClass(ItemStack.class);
        forbidClass(Gson.class);
        forbidClass(GsonBuilder.class);
        forbidClass(EnumBiMap.class);
        forbidClass(ModelLoader.class);
        forbidClass(Minecraft.class);
        forbidClass(BlockModelShapes.class);
        forbidClass(BlockFaceUV.class); // BlockPartFace handles it
        forbidClass(ModelManager.class);
        forbidClass(BlockPartRotation.class); // not handled
        forbidClass(ModelBlockAnimation.class); // not handled
        forbidClass(BufferBuilder.class);
        forbidClass(SoundHandler.class);
        forbidClass(SoundManager.class);
        forbidClass(GameSettings.class);

        forbidClass(Logger.class);
        forbidClass(Joiner.class);
        forbidClass(Tessellator.class);
        forbidClass(Cache.class);
        forbidClass(LoadingCache.class);
        forbidClass(VertexFormatElement.class);
    }

    private boolean shouldCheckClass(Class c) {
        Boolean result = SHOULD_PROCESS_CLASS.get(c);
        if (result == null) {
            if (c.isPrimitive() || c.isEnum() || (c.isArray() && !shouldCheckClass(c.getComponentType()))) {
                result = false;
            } else if (BakedQuad.class.isAssignableFrom(c)) {
                result = false;
            } else if (Item.class.isAssignableFrom(c) || Block.class.isAssignableFrom(c) || World.class.isAssignableFrom(c)
                    || Entity.class.isAssignableFrom(c) || Logger.class.isAssignableFrom(c) || IRegistry.class.isAssignableFrom(c)
                    || SimpleReloadableResourceManager.class.isAssignableFrom(c) || IResourcePack.class.isAssignableFrom(c)
                    || IProperty.class.isAssignableFrom(c) || IUnlistedProperty.class.isAssignableFrom(c)
                    || IRecipe.class.isAssignableFrom(c) || Ingredient.class.isAssignableFrom(c) || IBlockState.class.isAssignableFrom(c)
                    || LanguageManager.class.isAssignableFrom(c) || BlockPos.MutableBlockPos.class.isAssignableFrom(c)) {
                result = false;
            } else if (ItemOverrideList.class.isAssignableFrom(c)) {
                result = false;
            } else {
                result = true;
            }
            SHOULD_PROCESS_CLASS.put(c, result);
        }
        return result;
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
        Deduplicator0Function FLOATA_DEDUP = DEDUPLICATOR_0_FUNCTIONS.get(float[].class);

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
        Deduplicator0Function FLOATAA_DEDUP = DEDUPLICATOR_0_FUNCTIONS.get(float[][].class);

        DEDUPLICATOR_0_FUNCTIONS.put(float[][][].class, (o) -> {
            float[][][] arr = (float[][][]) o;
            for (int i = 0; i < arr.length; i++) {
                float[][] n = (float[][]) FLOATAA_DEDUP.deduplicate(arr[i]);
                if (n != arr[i]) successfuls++;
                arr[i] = n;
            }
            return arr;
        });
        Deduplicator0Function FLOATAAA_DEDUP = DEDUPLICATOR_0_FUNCTIONS.get(float[][][].class);

        DEDUPLICATOR_0_FUNCTIONS.put(ResourceLocation.class, RESOURCE_LOCATION_STORAGE::deduplicate);
        for (Class c : Lists.newArrayList(ModelResourceLocation.class, Vec3d.class, Vec3i.class, BlockPos.class, TRSRTransformation.class,
                FoamyConditionPropertyValue.PredicateNegative.class, FoamyConditionPropertyValue.PredicatePositive.class, FoamyConditionPropertyValue.class,
                FoamyConditionOr.PredicateImpl.class, FoamyConditionAnd.PredicateImpl.class,
                FoamyConditionPropertyValue.SingletonPredicateNegative.class, FoamyConditionPropertyValue.SingletonPredicatePositive.class)) {
            final IDeduplicatingStorage<Object> OBJECT_STORAGE = new DeduplicatingStorageTrove<>(HashingStrategies.GENERIC);
            DEDUPLICATOR_0_FUNCTIONS.put(c, OBJECT_STORAGE::deduplicate);
        }

        {
            final IDeduplicatingStorage<FoamyMultipartBakedModel> FOAMY_MULTIPART_STORAGE = new DeduplicatingStorageTrove<>(new FoamyMultipartBakedModelHashingStrategy());
            DEDUPLICATOR_0_FUNCTIONS.put(FoamyMultipartBakedModel.class, (obj) -> FOAMY_MULTIPART_STORAGE.deduplicate((FoamyMultipartBakedModel) obj));
        }

        if (FoamFixShared.isCoremod) {
            DEDUPLICATOR_0_FUNCTIONS.put(Style.class, obj -> deduplicateStyleIfCoremodPresent((Style) obj));
        } else {
            DEDUPLICATOR_0_FUNCTIONS.put(Style.class, obj -> obj);
        }

        DEDUPLICATOR_0_FUNCTIONS.put(ItemCameraTransforms.class, obj -> ICT_STORAGE.deduplicate((ItemCameraTransforms) obj));

        DEDUPLICATOR_FUNCTIONS.put(BlockPartFace.class, (o, recursion) -> {
            float[] n = (float[]) FLOATA_DEDUP.deduplicate(((BlockPartFace) o).blockFaceUV.uvs);
            if (n != ((BlockPartFace) o).blockFaceUV.uvs) {
                successfuls++;
            }
            return o;
        });

        if (FoamFixShared.config.expUnpackBakedQuads) {
            DEDUPLICATOR_FUNCTIONS.put(BakedQuad.class, (o, recursion) -> {
                BakedQuad quad = (BakedQuad) o;
                UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(quad.getFormat());
                quad.pipe(builder);
                o = builder.build();
                return o;
            });
        } else {
            DEDUPLICATOR_FUNCTIONS.put(BakedQuad.class, (o, recursion) -> o);
        }
        DEDUPLICATOR_FUNCTIONS.put(UnpackedBakedQuad.class, (o, recursion) -> {
            try {
                float[][][] array = (float[][][]) FIELD_UNPACKED_DATA_GETTER.invokeExact((UnpackedBakedQuad) o);
                FLOATAAA_DEDUP.deduplicate(array);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return o;
        });

        DEDUPLICATOR_FUNCTIONS.put(ItemOverrideList.class, (obj, recursion) -> {
            if (obj != ItemOverrideList.NONE) {
                List list = (List) IOL_OVERRIDES_GETTER.invokeExact((ItemOverrideList) obj);
                if (list.isEmpty()) {
                    successfuls++;
                    return ItemOverrideList.NONE;
                }
            }
            return obj;
        });

        DEDUPLICATOR_FUNCTIONS.put(AnimationItemOverrideList.class, (obj, recursion) -> {
            List list = (List) IOL_OVERRIDES_GETTER.invokeExact((ItemOverrideList) obj);
            if (list.isEmpty()) {
                successfuls++;
                IOL_OVERRIDES_SETTER.invokeExact((ItemOverrideList) obj, (List) ImmutableList.of());
            }
            return obj;
        });

        for (Class c : DEDUPLICATOR_FUNCTIONS.keySet()) {
            permitClass(c);
        }

        addClassFromName(DEDUPLICATOR_FUNCTIONS, RegularImmutableListDeduplicatorFunction.CLASS_NAME, new RegularImmutableListDeduplicatorFunction(this));
        addClassFromName(DEDUPLICATOR_FUNCTIONS, SingletonImmutableBiMapDeduplicatorFunction.CLASS_NAME, new SingletonImmutableBiMapDeduplicatorFunction(this));
        addClassFromName(DEDUPLICATOR_FUNCTIONS, SingletonImmutableListDeduplicatorFunction.CLASS_NAME, new SingletonImmutableListDeduplicatorFunction(this));
        addClassFromName(DEDUPLICATOR_FUNCTIONS, SingletonImmutableSetDeduplicatorFunction.CLASS_NAME, new SingletonImmutableSetDeduplicatorFunction(this));
    }

    private Deduplicator0Function getDeduplicate0Func(Class c) {
        Deduplicator0Function func = DEDUPLICATOR_0_FUNCTIONS.get(c);
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

    private static final boolean cSetProp;

    static {
        String s = System.getProperty("pl.asie.foamfix.debugDeduplicatedClasses", "");
        cSetProp = !s.isEmpty();
    }

    private DeduplicatorFunction createDeduplicatorFunction(Class c) {
        DeduplicatorFunction func = null;
        boolean isIdentity = false;
        boolean continueProcessing = true;

        if (Reference.class.isAssignableFrom(c)) {
            func = (obj, recursion) -> deduplicateObject(((Reference) obj).get(), recursion + 1);
            continueProcessing = false;
        } else if (IBakedModel.class.isAssignableFrom(c)) {
            Deduplicator0Function immMapFunc = getDeduplicate0Func(ImmutableMap.class);

            if (PerspectiveMapWrapper.class.isAssignableFrom(c)) {
                func = (obj, recursion) -> {
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
                func = (obj, recursion) -> {
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
                func = (obj, recursion) -> {
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        List l = ((IBakedModel) obj).getQuads(null, facing, 0);
                        trimArray(l);
                    }
                    return obj;
                };
            }
        } else if (java.util.Optional.class.isAssignableFrom(c)) {
            func = (obj, recursion) -> {
                java.util.Optional opt = (java.util.Optional) obj;
                if (opt.isPresent()) {
                    Object b = deduplicateObject(opt.get(), recursion + 1);
                    if (b != null) {
                        java.util.Optional optCached = JAVA_OPTIONALS.get(b);
                        if (optCached != null) {
                            successfuls++;
                            return optCached;
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
        } else if (com.google.common.base.Optional.class.isAssignableFrom(c)) {
            func = (obj, recursion) -> {
                Optional opt = (Optional) obj;
                if (opt.isPresent()) {
                    Object b = deduplicateObject(opt.get(), recursion + 1);
                    if (b != null) {
                        Optional optCached = GUAVA_OPTIONALS.get(b);
                        if (optCached != null) {
                            successfuls++;
                            return optCached;
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
        } else if (Multimap.class.isAssignableFrom(c)) {
            if (ImmutableMultimap.class.isAssignableFrom(c) || SortedSetMultimap.class.isAssignableFrom(c)) {
                func = (obj, recursion) -> {
                    for (Object value : ((Multimap) obj).values()) {
                        deduplicateObject(value, recursion + 1);
                    }
                    return obj;
                };
            } else {
                func = (obj, recursion) -> {
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
        } else if (Map.class.isAssignableFrom(c)) {
            if (SortedMap.class.isAssignableFrom(c) || IMMUTABLE_CLASS.contains(c)) {
                func = (obj, recursion) -> {
                    for (Object v : ((Map) obj).keySet()) {
                        deduplicateObject(v, recursion + 1);
                    }

                    for (Object v : ((Map) obj).values()) {
                        deduplicateObject(v, recursion + 1);
                    }
                    return obj;
                };
            } else if (ImmutableBiMap.class.isAssignableFrom(c)) {
                func = (obj, recursion) -> {
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
            } else if (ImmutableMap.class.isAssignableFrom(c)) {
                func = (obj, recursion) -> {
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
                func = (obj, recursion) -> {
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
        } else if (Collection.class.isAssignableFrom(c)) {
            if (List.class.isAssignableFrom(c)) {
                if (IMMUTABLE_CLASS.contains(c)) {
                    func = (obj, recursion) -> {
                        List l = (List) obj;
                        for (int i = 0; i < l.size(); i++) {
                            deduplicateObject(l.get(i), recursion + 1);
                        }
                        return obj;
                    };
                } else if (ImmutableList.class.isAssignableFrom(c)) {
                    func = (obj, recursion) -> {
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
                    func = (obj, recursion) -> {
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
            } else if (ImmutableSet.class.isAssignableFrom(c)) {
                if (!(ImmutableSortedSet.class.isAssignableFrom(c))) {
                    func = (obj, recursion) -> {
                        ImmutableSet.Builder builder = new ImmutableSet.Builder();
                        for (Object o1 : ((Set) obj)) {
                            builder.add(deduplicateObject(o1, recursion + 1));
                        }
                        return builder.build();
                    };
                } else {
                    func = (obj, recursion) -> {
                        for (Object o1 : ((Set) obj)) {
                            deduplicateObject(o1, recursion + 1);
                        }
                        return obj;
                    };
                }
            } else {
                if (Set.class.isAssignableFrom(c) && !(SortedSet.class.isAssignableFrom(c))) {
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
                                func = (obj, recursion) -> {
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
                    func = (obj, recursion) -> {
                        for (Object o1 : ((Collection) obj)) {
                            deduplicateObject(o1, recursion + 1);
                        }
                        return obj;
                    };
                }
            }
            continueProcessing = false;
        } else if (c.isArray()) {
            func = (obj, recursion) -> {
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
            Deduplicator0Function d0func = getDeduplicate0Func(c);
            if (d0func == null) {
                isIdentity = true;
                func = (obj, recursion) -> obj;
            } else {
                func = (obj, recursion) -> {
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
            boolean canTrim = Predicate.class.isAssignableFrom(c) || TRIM_ARRAYS_CLASSES.contains(c);
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            if (cSetProp) {
                Class cc = c;
                do {
                    for (Field f : cc.getDeclaredFields()) {
                        if ((f.getModifiers() & Modifier.STATIC) != 0)
                            continue;

                        if (shouldCheckClass(f.getType())) {
                            System.out.println("-> " + f.getType().getName());
                        }
                    }
                } while ((cc = cc.getSuperclass()) != Object.class);
            }

            ImmutableList.Builder<MethodHandle> fsBuilder = ImmutableList.builder();
            {
                Class cc = c;
                do {
                    for (Field f : cc.getDeclaredFields()) {
                        if ((f.getModifiers() & Modifier.STATIC) != 0)
                            continue;

                        if (shouldCheckClass(f.getType())) {
                            try {
                                f.setAccessible(true);
                                fsBuilder.add(lookup.unreflectGetter(f));
                                fsBuilder.add(lookup.unreflectSetter(f));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } while ((cc = cc.getSuperclass()) != Object.class);
            }
            MethodHandle[] methodHandles = fsBuilder.build().toArray(new MethodHandle[0]);

            if (methodHandles.length > 0) {
                final DeduplicatorFunction oldFunc = func;
                if (canTrim) {
                    func = (obj, recursion) -> {
                        obj = oldFunc.deduplicate(obj, recursion);
                        for (int i = 0; i < methodHandles.length; i += 2) {
                            try {
                                // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                                Object value = methodHandles[i].invoke(obj);
                                Object valueD = deduplicateObject(value, recursion + 1);

                                if (valueD != null) {
                                    trimArray(valueD);
                                    if (valueD != value) {
                                        methodHandles[i + 1].invoke(obj, valueD);
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
                    func = (obj, recursion) -> {
                        obj = oldFunc.deduplicate(obj, recursion);
                        for (int i = 0; i < methodHandles.length; i += 2) {
                            try {
                                // System.out.println("-" + Strings.repeat("-", recursion) + "* " + f.getName());
                                Object value = methodHandles[i].invoke(obj);
                                Object valueD = deduplicateObject(value, recursion + 1);

                                if (valueD != null) {
                                    if (valueD != value) {
                                        methodHandles[i + 1].invoke(obj, valueD);
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
            } else if (isIdentity) {
                forbidClass(c);
            }
        }

        return func;
    }

    public Object deduplicateObject(Object o, int parentRecursion) {
        if (o == null || parentRecursion > maxRecursion)
            return o;

        Class c = o.getClass();
        if (!shouldCheckClass(c))
            return o;

        if (!deduplicatedObjects.add(o))
            return o;

        DeduplicatorFunction func = DEDUPLICATOR_FUNCTIONS.get(c);
        if (func == null) {
            if (cSetProp) {
                System.out.println(Strings.repeat("-", parentRecursion) + "- " + c.getName());
            }
            func = createDeduplicatorFunction(c);
            DEDUPLICATOR_FUNCTIONS.put(c, func);
        }

        try {
            return func.deduplicate(o, parentRecursion);
        } catch (Throwable t) {
            return o;
        }
    }
}
