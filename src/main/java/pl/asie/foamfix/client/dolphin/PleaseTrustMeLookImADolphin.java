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
package pl.asie.foamfix.client.dolphin;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class PleaseTrustMeLookImADolphin {
    public static final PleaseTrustMeLookImADolphin INSTANCE = new PleaseTrustMeLookImADolphin();
    File CANARY = new File("./.foamfix-dolphin");
    private boolean initialized = false;
    private Field gwls_f, lglwse_f, gws_f;
    private Field[] enabledButton_f, disabledButton_f;
    private String[] nameRestore, newNames;

    private PleaseTrustMeLookImADolphin() {

    }

    private void initialize() {
        if (!initialized) {
            if (Loader.isModLoaded("flamingo") || Loader.isModLoaded("Flamingo")) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }

            if (CANARY.exists()) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }

            gws_f = ReflectionHelper.findField(GuiListWorldSelection.class, "worldSelectionObj", "worldSelection", "field_186798_v");
            gwls_f = ReflectionHelper.findField(GuiWorldSelection.class, "selectionList", "field_184866_u");
            lglwse_f = ReflectionHelper.findField(GuiListWorldSelection.class, "entries", "field_186799_w");
            enabledButton_f = new Field[] {
                    ReflectionHelper.findField(GuiWorldSelection.class, "selectButton", "field_146641_z"),
                    ReflectionHelper.findField(GuiWorldSelection.class, "deleteButton", "field_146642_y"),
            };
            disabledButton_f = new Field[] {
                    ReflectionHelper.findField(GuiWorldSelection.class, "renameButton", "field_146630_A"),
                    ReflectionHelper.findField(GuiWorldSelection.class, "copyButton", "field_184865_t"),
            };
            nameRestore = new String[2];
            newNames = new String[] {
                    "Download Mod",
                    "Hide"
            };
            initialized = true;
        }
    }

    private void restore(GuiWorldSelection gws) {
        try {
            for (int i = 0; i < enabledButton_f.length; i++) {
                Field f = enabledButton_f[i];
                if (f != null && nameRestore[i] != null) {
                    ((GuiButton) f.get(gws)).displayString = nameRestore[i];
                    nameRestore[i] = null;
                }
            }
        } catch (Exception e) {

        }
    }

    @SuppressWarnings("unchecked")
    protected void disable(GuiListWorldSelection gwls, GuiListWorldSelectionEntry entry) {
        try {
            Files.write("no, look. i'm really a dolphin.", CANARY, Charsets.UTF_8);
            List<GuiListWorldSelectionEntry> lglwse = (List<GuiListWorldSelectionEntry>) lglwse_f.get(gwls);
            GuiWorldSelection gws = (GuiWorldSelection) gws_f.get(gwls);
            lglwse.remove(entry);
            restore(gws);
            gws.selectWorld(null);
            MinecraftForge.EVENT_BUS.unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onGuiActive(GuiScreenEvent.DrawScreenEvent.Pre event) {
        try {
            if (event.getGui() instanceof GuiWorldSelection) {
                initialize();

                GuiWorldSelection gws = (GuiWorldSelection) event.getGui();
                GuiListWorldSelection gwls = (GuiListWorldSelection) gwls_f.get(gws);
                List<GuiListWorldSelectionEntry> lglwse = (List<GuiListWorldSelectionEntry>) lglwse_f.get(gwls);

                if (gwls.getSelectedWorld() instanceof TotallyADolphinEntry) {
                    for (int i = 0; i < enabledButton_f.length; i++) {
                        Field f = enabledButton_f[i];
                        if (f != null) {
                            GuiButton button = ((GuiButton) f.get(gws));
                            button.enabled = true;
                            if (nameRestore[i] == null) {
                                nameRestore[i] = button.displayString;
                                button.displayString = newNames[i];
                            }
                        }
                    }
                    for (Field f : disabledButton_f) {
                        if (f != null) ((GuiButton) f.get(gws)).enabled = false;
                    }
                } else {
                    restore(gws);
                }

                for (GuiListWorldSelectionEntry entry : lglwse) {
                    if (entry instanceof TotallyADolphinEntry) {
                        return;
                    }
                }

                WorldInfo worldInfo = new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT), "Flamingo1");
                WorldSummary worldSummary = new WorldSummary(worldInfo, "Flamingo2", "Flamingo3", 0, false);
                TotallyADolphinEntry dolphin = new TotallyADolphinEntry(gwls, worldSummary, new TotallyADolphinSaveFormat(worldInfo));
                lglwse.add(dolphin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
