/**
 * This file is part of FoamFixAPI.
 *
 * FoamFixAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFixAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFixAPI.  If not, see <http://www.gnu.org/licenses/>.
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

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.IRegistry;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.ProxyClient;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by asie on 11/22/16.
 */
public class FoamFixModelRegistryDuplicateWipe {
    @SubscribeEvent
    public void onTextureStitchPost(TextureStitchEvent.Post event) {
        ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        BlockModelShapes bms = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        ModelManager mgr = bms.getModelManager();

        Field f = ReflectionHelper.findField(ModelManager.class, "modelRegistry", "field_174958_a");
        try {
            RegistrySimple<ModelResourceLocation, IBakedModel> registry = (RegistrySimple<ModelResourceLocation, IBakedModel>) f.get(mgr);
            FoamFix.logger.info("Clearing unnecessary model registry of size " + registry.getKeys().size() + ".");
            for (ModelResourceLocation l : registry.getKeys()) {
                registry.putObject(l, ProxyClient.DUMMY_MODEL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        f = ReflectionHelper.findField(BlockModelShapes.class, "bakedModelStore", "field_178129_a");
        try {
            Map<IBlockState, IBakedModel> modelStore = (Map<IBlockState, IBakedModel>) f.get(bms);
            FoamFix.logger.info("Clearing unnecessary model store of size " + modelStore.size() + ".");
            modelStore.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imm instanceof ItemModelMesherForge) {
            f = ReflectionHelper.findField(ItemModelMesherForge.class, "models");
            try {
                IdentityHashMap<Item, TIntObjectHashMap<IBakedModel>> modelStore = (IdentityHashMap<Item, TIntObjectHashMap<IBakedModel>>) f.get(imm);
                FoamFix.logger.info("Clearing unnecessary item shapes cache of size " + modelStore.size() + ".");
                modelStore.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
