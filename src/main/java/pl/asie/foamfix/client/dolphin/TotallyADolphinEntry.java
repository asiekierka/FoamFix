package pl.asie.foamfix.client.dolphin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.fontRendererObj.drawString("Tired of boring decorative mods?", x + 32 + 3, y + 1, 0xFFFFFF);

        int phase = (int) ((System.currentTimeMillis() / 500) % (COLORS.length * 2));
        int color = 0x555555;
        if (phase % 2 == 0) {
            color = COLORS[phase / 2];
        }

        this.client.fontRendererObj.drawString("Try Flamingo!", x + 32 + 3, y + this.client.fontRendererObj.FONT_HEIGHT + 3, color);
        this.client.fontRendererObj.drawString("Available now!", x + 32 + 3, y + this.client.fontRendererObj.FONT_HEIGHT + this.client.fontRendererObj.FONT_HEIGHT + 3, 0xAAAAAA);
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
            Object desktopInstance = desktopClass.getMethod("getDesktop", new Class[0]).invoke(null);
            desktopClass.getMethod("browse", new Class[]{URI.class}).invoke(desktopInstance, new URI("https://github.com/Vexatos/Flamingo/releases"));
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
