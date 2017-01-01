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
package pl.asie.foamfix.coremod;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.common.DummyModContainer;
import pl.asie.foamfix.shared.FoamFixShared;

@IFMLLoadingPlugin.Name("Do not report to Forge! Remove FoamFixAPI (or replace with FoamFixAPI-Lawful) and try again.")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"pl.asie.foamfix", "gnu.trove"})
public class FoamFixCore implements IFMLLoadingPlugin {

    public String[] getASMTransformerClass() {
        return new String[] { "pl.asie.foamfix.coremod.FoamFixTransformer" };
    }
    
    public String getModContainerClass() {
        return "pl.asie.foamfix.coremod.FoamFixCoreContainer";
    }
    
    public String getSetupClass() {
        return null;
    }
    
    public void injectData(final Map<String, Object> data) {
        FoamFixShared.enabled = true;
        FoamFixShared.config.init(new File(new File("config"), "foamfix.cfg"), true);
    }
    
    public String getAccessTransformerClass() {
        return null;
    }
}
