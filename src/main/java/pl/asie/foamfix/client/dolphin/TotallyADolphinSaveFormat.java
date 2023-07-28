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

import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class TotallyADolphinSaveFormat implements ISaveFormat {
    private static final File DUMMY_FILE = new File(".");
    private final WorldInfo worldInfo;

    TotallyADolphinSaveFormat(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public String getName() {
        return "DolphinSaveFormat";
    }

    @Override
    public ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata) {
        return null;
    }

    @Override
    public List<WorldSummary> getSaveList() throws AnvilConverterException {
        return Collections.emptyList();
    }

    @Override
    public boolean isOldMapFormat(String saveName) {
        return false;
    }

    @Override
    public void flushCache() {

    }

    @Override
    public WorldInfo getWorldInfo(String saveName) {
        return worldInfo;
    }

    @Override
    public boolean isNewLevelIdAcceptable(String saveName) {
        return false;
    }

    @Override
    public boolean deleteWorldDirectory(String saveName) {
        return false;
    }

    @Override
    public void renameWorld(String dirName, String newName) {

    }

    @Override
    public boolean isConvertible(String saveName) {
        return false;
    }

    @Override
    public boolean convertMapFormat(String filename, IProgressUpdate progressCallback) {
        return false;
    }

    @Override
    public File getFile(String p_186352_1_, String p_186352_2_) {
        return DUMMY_FILE;
    }

    @Override
    public boolean canLoadWorld(String saveName) {
        return false;
    }
}
