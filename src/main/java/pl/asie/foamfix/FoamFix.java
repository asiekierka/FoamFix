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
package pl.asie.foamfix;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.util.Deduplicator;
import pl.asie.foamfix.util.FoamUtils;
import pl.asie.foamfix.util.PretendPackageMap;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Mod(modid = "foamfix", name = "FoamFix", version = "0.3.1", clientSideOnly = true, acceptedMinecraftVersions = "[1.10.2,1.11]")
public class FoamFix {
    public static final IBakedModel DUMMY_MODEL = new IBakedModel() {
        private final ItemOverrideList itemOverrideList = ItemOverrideList.NONE;

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return ImmutableList.of();
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemOverrideList;
        }
    };

    public static Deduplicator deduplicator = new Deduplicator();
    public static Configuration config;
    public static Logger logger;
    public static int stage;

    public static boolean lwWeakenResourceCache, lwDummyPackageManifestMap;
    public static boolean mcDeduplicate, mcCleanRedundantModelRegistry;

    private void optimizeLaunchWrapper() {
        LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();
        Field resourceCacheField = ReflectionHelper.findField(LaunchClassLoader.class, "resourceCache");
        Field packageManifestsField = ReflectionHelper.findField(LaunchClassLoader.class, "packageManifests");
        if (lwWeakenResourceCache) {
            FoamFix.logger.info("Weakening LaunchWrapper resource cache...");
            try {
                Map oldResourceCache = (Map) resourceCacheField.get(loader);
                Map newResourceCache = CacheBuilder.newBuilder().weakValues().build().asMap();
                newResourceCache.putAll(oldResourceCache);
                resourceCacheField.set(loader, newResourceCache);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (lwDummyPackageManifestMap) {
            FoamFix.logger.info("Dummying out LaunchWrapper's unused package manifests...");
            try {
                // Map<Package, Manifest> packageManifests = (Map) packageManifestsField.get(loader);
                packageManifestsField.set(loader, new PretendPackageMap());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = LogManager.getLogger("foamfix");
        stage = 0;
        config = new Configuration(event.getSuggestedConfigurationFile());

        lwDummyPackageManifestMap = config.getBoolean("dummyPackageManifestMap", "launchwrapper", true, "Dummy out LaunchWrapper's unused package manifest map. This will only break things if some other mod reflects into the LaunchClassLoader to get the private map, which as far as I know is not the case.");
        lwWeakenResourceCache = config.getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.");
        mcDeduplicate = config.getBoolean("deduplicate", "general", true, "Enable deduplication of redundant objects in memory.");
        mcCleanRedundantModelRegistry = config.getBoolean("clearDuplicateModelRegistry", "general", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.");

        config.save();

        if (!mcDeduplicate) {
            deduplicator = null;
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        stage = 1;
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new FoamFixModelDeduplicate());
        if (mcCleanRedundantModelRegistry) {
            MinecraftForge.EVENT_BUS.register(new FoamFixModelRegistryDuplicateWipe());
        }
        optimizeLaunchWrapper();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (deduplicator != null) {
            deduplicator.successfuls = 0;
        }
        stage = 2;
    }
}
