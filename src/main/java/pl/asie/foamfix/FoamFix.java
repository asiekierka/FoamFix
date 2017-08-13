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
package pl.asie.foamfix;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.api.FoamFixAPI;
import pl.asie.foamfix.common.FoamFixHelper;
import pl.asie.foamfix.shared.FoamFixShared;

import java.text.DecimalFormat;

@Mod(modid = "foamfix", name = "FoamFix", version = "@VERSION@", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12,1.13)",
guiFactory = "pl.asie.foamfix.client.gui.FoamFixGuiFactory")
public class FoamFix {
    private static Item AIR;

    @SidedProxy(clientSide = "pl.asie.foamfix.ProxyClient", serverSide = "pl.asie.foamfix.ProxyCommon", modId = "foamfix")
    public static ProxyCommon proxy;

    public static Logger logger;
    public static int stage;

    public static Item getItemAir() {
        return AIR == null ? (AIR = Item.getItemFromBlock(Blocks.AIR)) : AIR;
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        AIR = Item.getItemFromBlock(Blocks.AIR);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FoamFixAPI.HELPER = new FoamFixHelper();

        logger = LogManager.getLogger("foamfix");
        stage = 0;

        FoamFixShared.config.init(event.getSuggestedConfigurationFile(), false);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        stage = 1;
        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        stage = 2;
        proxy.postInit();

        if (FoamFixShared.config.twDisableRedstoneLight) {
            Blocks.REDSTONE_TORCH.setLightLevel(0.0f);
            Blocks.POWERED_REPEATER.setLightLevel(0.0f);
            Blocks.POWERED_COMPARATOR.setLightLevel(0.0f);
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {

    }

    @SubscribeEvent
    public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("foamfix".equals(event.getModID())) {
            FoamFixShared.config.reload();

            if (FoamFixShared.config.resourceDirty) {
                proxy.refreshResources();
                FoamFixShared.config.resourceDirty = false;
            }
        }
    }

    private static final DecimalFormat RAM_SAVED_DF = new DecimalFormat("0.#");

    public static void updateRamSaved() {
//        logger.info("So far, FoamFixAPI saved you (at least, approximately - guessing a bit here) " + RAM_SAVED_DF.format((FoamFixShared.ramSaved / 1048576.0f)) + " MB! (Note that not every optimization can be counted here.)");
    }
}
