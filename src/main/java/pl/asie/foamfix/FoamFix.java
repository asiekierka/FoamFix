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

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.shared.FoamFixShared;

@Mod(modid = "foamfix", name = "FoamFix", version = "0.4.1", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.10.2,1.11]")
public class FoamFix {
    @SidedProxy(clientSide = "pl.asie.foamfix.ProxyClient", serverSide = "pl.asie.foamfix.ProxyCommon", modId = "foamfix")
    public static ProxyCommon proxy;

    public static Logger logger;
    public static int stage;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
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
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {

    }
}
