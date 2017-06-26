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
                ConfigCategory category = c.getCategory(s);
                for (Property p : category.values()) {
                    if (FoamFixShared.config.isApplicable(p) && p.showInGui() && p.requiresMcRestart() == restartRequired) {
                        elements.add(new ConfigElement(p));
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
