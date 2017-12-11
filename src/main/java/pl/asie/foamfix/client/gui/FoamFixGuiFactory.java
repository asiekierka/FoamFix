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

package pl.asie.foamfix.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FoamFixGuiFactory implements IModGuiFactory {
    public static class ConfigGui extends GuiConfig {
        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), "foamfix", "FoamFix", false, false, I18n.format("FoamFix Configuration"));
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> elements = new ArrayList<>();

            elements.add(new DummyConfigElement.DummyCategoryElement("Load Time Options (Restart required)", "foamfix.config.restartRequired", getChildElements(true)));
            elements.addAll(getChildElements(false));

            return elements;
        }

        private static List<IConfigElement> getChildElements(boolean restartRequired) {
            List<IConfigElement> elements = new ArrayList<>();
            Configuration c = FoamFixShared.config.getConfig();

            for (String s : c.getCategoryNames()) {
                boolean isExperimental = "experimental".equals(s);
                ConfigCategory category = c.getCategory(s);
                for (Property p : category.values()) {
                    if (FoamFixShared.config.isApplicable(p) && p.showInGui() && p.requiresMcRestart() == restartRequired) {
                        if (isExperimental) {
                            elements.add(new ConfigElement(p) {
                                @Override
                                public String getName() {
                                    return "[EXPERIMENTAL] " + super.getName();
                                }
                            });
                        } else {
                            elements.add(new ConfigElement(p) {
                                @Override
                                public String getName() {
                                    return category.getName() + "." + super.getName();
                                }
                            });
                        }
                    }
                }
            }

            return elements;
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
