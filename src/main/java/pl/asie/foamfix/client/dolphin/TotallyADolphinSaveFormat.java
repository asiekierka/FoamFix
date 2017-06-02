package pl.asie.foamfix.client.dolphin;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
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
