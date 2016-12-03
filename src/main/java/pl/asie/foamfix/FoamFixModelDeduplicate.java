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

import net.minecraftforge.fml.common.ProgressManager;
import pl.asie.foamfix.util.Deduplicator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.foamfix.util.FoamUtils;

public class FoamFixModelDeduplicate {
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        // TODO: analyze impact + obj. references
        // TODO: figure out why it breaks Botania (#1, refer to vazkii/botania/client/model/FloatingFlowerModel.java)
        //FoamUtils.wipeModelLoaderRegistryCache();

        if (FoamFix.mcDeduplicate) {
            ProgressManager.ProgressBar bakeBar = ProgressManager.push("FoamFix: deduplicating", event.getModelRegistry().getKeys().size());

            if (FoamFix.deduplicator == null) {
                FoamFix.deduplicator = new Deduplicator();
            }

            FoamFix.logger.info("Deduplicating models...");
            FoamFix.deduplicator.maxRecursion = 6;

            for (ModelResourceLocation loc : event.getModelRegistry().getKeys()) {
                IBakedModel model = event.getModelRegistry().getObject(loc);
                String modelName = loc.toString();
                bakeBar.step(String.format("[%s]", modelName));
                try {
                    FoamFix.deduplicator.deduplicateObject(model, 0);
                } catch (Exception e) {

                }
            }

            ProgressManager.pop(bakeBar);
            FoamFix.logger.info("Deduplicated " + FoamFix.deduplicator.successfuls + " objects.");
        }

        FoamFix.deduplicator = null; // release deduplicator to save memory
    }
}
