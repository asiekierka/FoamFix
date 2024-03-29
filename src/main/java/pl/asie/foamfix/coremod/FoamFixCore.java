/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import pl.asie.foamfix.shared.FoamFixShared;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@IFMLLoadingPlugin.Name("Do not report to Forge! (If you haven't disabled the FoamFix coremod, try disabling it in the config! Note that this bit of text will still appear.)")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"pl.asie.foamfix"})
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
        FoamFixShared.config.init(new File(new File("config"), "foamfix.cfg"), true);

        if (FoamFixShared.isCoremod) {
            if (FoamFixShared.config.clInitOptions) {
                try {
                    File optionsFile = new File("options.txt");
                    if (!optionsFile.exists()) {
                        Files.write("mipmapLevels:0\n", optionsFile, Charsets.UTF_8);
                    }
                    File forgeCfgFile = new File(new File("config"), "forge.cfg");
                    if (!forgeCfgFile.exists()) {
                        Files.write("client {\nB:alwaysSetupTerrainOffThread=true\n}\n", forgeCfgFile, Charsets.UTF_8);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (getClass().getClassLoader() instanceof LaunchClassLoader) {
	            if (FoamFixShared.config.geBlacklistLibraryTransformers) {
		            LaunchClassLoader classLoader = (LaunchClassLoader) getClass().getClassLoader();
		            classLoader.addTransformerExclusion("com.ibm.icu.");
		            classLoader.addTransformerExclusion("com.sun.");
		            classLoader.addTransformerExclusion("gnu.trove.");
		            classLoader.addTransformerExclusion("io.netty.");
		            classLoader.addTransformerExclusion("it.unimi.dsi.fastutil.");
		            classLoader.addTransformerExclusion("joptsimple.");
		            classLoader.addTransformerExclusion("org.apache.");
		            classLoader.addTransformerExclusion("oshi.");
		            classLoader.addTransformerExclusion("scala.");
	            }

	            LaunchClassLoader classLoader = (LaunchClassLoader) getClass().getClassLoader();

	            // Not so simple!
/*            try {
                Field transformersField = ReflectionHelper.findField(LaunchClassLoader.class, "transformers");
                List<IClassTransformer> transformerList = (List<IClassTransformer>) transformersField.get(classLoader);

                for (int i = 0; i < transformerList.size(); i++) {
                    IClassTransformer transformer = transformerList.get(i);
                    IClassTransformer parentTransformer = transformer;
                    if (transformer instanceof ASMTransformerWrapper.TransformerWrapper) {
                        Field parentTransformerField = ReflectionHelper.findField(ASMTransformerWrapper.TransformerWrapper.class, "parent");
                        parentTransformer = (IClassTransformer) parentTransformerField.get(transformer);
                    }
                if (parentTransformer instanceof SideTransformer) {
                    if (FoamFixShared.config.geFasterSideTransformer) {
                        transformerList.set(i, new FoamySideTransformer());
                    }
                }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } */
            }

            FoamFixTransformer.init();
        }
    }
    
    public String getAccessTransformerClass() {
        return "pl.asie.foamfix.coremod.FoamFixAT";
    }
}
