package pl.asie.foamfix.coremod.injections;

import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import pl.asie.foamfix.coremod.patches.IFoamFixWorldRemovable;

public class WorldRemovalInject extends World implements IFoamFixWorldRemovable {
    protected WorldRemovalInject(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    @Override
    public void foamfix_removeUnloadedEntities() {
        this.profiler.startSection("entities");
        this.profiler.startSection("remove");

        if (!this.unloadedEntityList.isEmpty()) {
            this.loadedEntityList.removeAll(this.unloadedEntityList);
            int i;

            for (Entity entity : unloadedEntityList) {
                int j = entity.chunkCoordX;
                int k1 = entity.chunkCoordZ;

                if (entity.addedToChunk && this.isChunkLoaded(j, k1, true)) {
                    this.getChunkFromChunkCoords(j, k1).removeEntity(entity);
                }
            }

            for (Entity entity : unloadedEntityList) {
                this.onEntityRemoved(entity);
            }

            this.unloadedEntityList.clear();
        }

        this.profiler.endStartSection("blockEntities");

        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            for (TileEntity tile : tileEntitiesToBeRemoved) {
                tile.onChunkUnload();
            }

            this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
            this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
            this.tileEntitiesToBeRemoved.clear();
        }

        this.profiler.endSection();
        this.profiler.endSection();
    }

}
