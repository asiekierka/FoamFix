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

package pl.asie.foamfix.client.dolphin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldSummary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

public class TotallyADolphinEntry extends GuiListWorldSelectionEntry {
    private static final int[] COLORS = {
            0xFF5555,
            0xFFAA00,
            0xFFFF55,
            0x55FF55,
            0x55FFFF,
            0x5555FF,
            0xFF55FF
    };
    private GuiListWorldSelection list;
    private Minecraft client;

    public TotallyADolphinEntry(GuiListWorldSelection listWorldSelIn, WorldSummary p_i46591_2_, ISaveFormat p_i46591_3_) {
        super(listWorldSelIn, p_i46591_2_, p_i46591_3_);
        this.list = listWorldSelIn;
        this.client = Minecraft.getMinecraft();
    }

    /* 1.10.2 - 1.11.2 */
    public void func_180790_a(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected,0);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_192634_9_) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.fontRenderer.drawString("Tired of boring decorative mods?", x + 32 + 3, y + 1, 0xFFFFFF);

        int phase = (int) ((System.currentTimeMillis() / 500) % (COLORS.length * 2));
        int color = 0x555555;
        if (phase % 2 == 0) {
            color = COLORS[phase / 2];
        }

        this.client.fontRenderer.drawString("Try Flamingo!", x + 32 + 3, y + this.client.fontRenderer.FONT_HEIGHT + 3, color);
        this.client.fontRenderer.drawString("Available now!", x + 32 + 3, y + this.client.fontRenderer.FONT_HEIGHT + this.client.fontRenderer.FONT_HEIGHT + 3, 0xAAAAAA);
        this.client.getTextureManager().bindTexture(new ResourceLocation("foamfix", "flamingo.png"));
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        if (this.client.gameSettings.touchscreen || isSelected) {
            Gui.drawRect(x,y,x+32,y+32, 0x80AAAAAA/*AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA*/);
        }
        GlStateManager.disableBlend();
    }

    @Override
    public void joinWorld() {
        try {
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            Object desktopInstance = desktopClass.getMethod("getDesktop").invoke(null);
            desktopClass.getMethod("browse", URI.class).invoke(desktopInstance, new URI("https://github.com/Vexatos/Flamingo/releases"));
        } catch (InvocationTargetException e) {
            // Did you think you can run away, Linux users?
            if (Util.getOSType() == Util.EnumOS.LINUX || Util.getOSType() == Util.EnumOS.SOLARIS) {
                try {
                    Runtime.getRuntime().exec("xdg-open https://github.com/Vexatos/Flamingo/releases");
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        } catch (Throwable throwable1) {
            throwable1.printStackTrace();
        }
    }

    @Override
    public void deleteWorld() {
        PleaseTrustMeLookImADolphin.INSTANCE.disable(list, this);
    }
}
