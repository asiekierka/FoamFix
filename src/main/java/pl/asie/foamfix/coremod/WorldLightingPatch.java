package pl.asie.foamfix.coremod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class WorldLightingPatch extends World {
	int[] lightUpdateBlockList;

	protected WorldLightingPatch(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
		super(saveHandlerIn, info, providerIn, profilerIn, client);
	}

	private int getRawLight(BlockPos pos, EnumSkyBlock lightType) {
		return 0;
	}

	// TODO: Actually optimize (for now, we just mutablify some BlockPos objects)
	@Override
	public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
		if (!this.isAreaLoaded(pos, 17, false)) {
			return false;
		} else {
			int checkedPosition = 0;
			int toCheckCount = 0;
			this.theProfiler.startSection("getBrightness");
			int currLight = this.getLightFor(lightType, pos);
			int newLight = this.getRawLight(pos, lightType);
			int posX = pos.getX();
			int posY = pos.getY();
			int posZ = pos.getZ();

			BlockPos.PooledMutableBlockPos checkedPos = BlockPos.PooledMutableBlockPos.retain();

			if (newLight > currLight) {
				this.lightUpdateBlockList[toCheckCount++] = 133152;
			} else if (newLight < currLight) {
				this.lightUpdateBlockList[toCheckCount++] = 133152 | currLight << 18;

				while (checkedPosition < toCheckCount) {
					int checkedEntry = this.lightUpdateBlockList[checkedPosition++];
					int checkedX = (checkedEntry & 63) - 32 + posX;
					int checkedY = (checkedEntry >> 6 & 63) - 32 + posY;
					int checkedZ = (checkedEntry >> 12 & 63) - 32 + posZ;
					checkedPos.setPos(checkedX, checkedY, checkedZ);

					int checkedNewLight = checkedEntry >> 18 & 15;
					int checkedCurrLight = this.getLightFor(lightType, checkedPos);

					if (checkedCurrLight == checkedNewLight) {
						this.setLightFor(lightType, checkedPos, 0);

						if (checkedNewLight > 0) {
							int distX = MathHelper.abs_int(checkedX - posX);
							int distY = MathHelper.abs_int(checkedY - posY);
							int distZ = MathHelper.abs_int(checkedZ - posZ);
							boolean hasSpace = toCheckCount < this.lightUpdateBlockList.length - 6;

							if (distX + distY + distZ < 17 && hasSpace) {
								BlockPos.PooledMutableBlockPos neighborPos = BlockPos.PooledMutableBlockPos.retain();

								for (EnumFacing enumfacing : EnumFacing.VALUES) {
									int neighborX = checkedX + enumfacing.getFrontOffsetX();
									int neighborY = checkedY + enumfacing.getFrontOffsetY();
									int neighborZ = checkedZ + enumfacing.getFrontOffsetZ();
									neighborPos.setPos(neighborX, neighborY, neighborZ);
									IBlockState neighborState = this.getBlockState(neighborPos);

									int l4 = Math.max(1, neighborState.getBlock().getLightOpacity(neighborState, this, neighborPos));
									checkedCurrLight = this.getLightFor(lightType, neighborPos);

									if (checkedCurrLight == checkedNewLight - l4) {
										this.lightUpdateBlockList[toCheckCount++] = neighborX - posX + 32 | neighborY - posY + 32 << 6 | neighborZ - posZ + 32 << 12 | checkedNewLight - l4 << 18;
									}
								}

								neighborPos.release();
							}
						}
					}
				}

				checkedPosition = 0;
			}

			this.theProfiler.endSection();
			this.theProfiler.startSection("checkedPosition < toCheckCount");

			while (checkedPosition < toCheckCount) {
				int checkedEntry = this.lightUpdateBlockList[checkedPosition++];
				int checkedX = (checkedEntry & 63) - 32 + posX;
				int checkedY = (checkedEntry >> 6 & 63) - 32 + posY;
				int checkedZ = (checkedEntry >> 12 & 63) - 32 + posZ;
				checkedPos.setPos(checkedX, checkedY, checkedZ);

				int checkedNewLight = this.getRawLight(checkedPos, lightType);
				int checkedCurrLight = this.getLightFor(lightType, checkedPos);

				if (checkedNewLight != checkedCurrLight) {
					this.setLightFor(lightType, checkedPos, checkedNewLight);

					if (checkedNewLight > checkedCurrLight) {
						int distX = Math.abs(checkedX - posX);
						int distY = Math.abs(checkedY - posY);
						int distZ = Math.abs(checkedZ - posZ);
						boolean hasSpace = toCheckCount < this.lightUpdateBlockList.length - 6;

						if (distX + distY + distZ < 17 && hasSpace) {
							if (this.getLightFor(lightType, checkedPos.setPos(checkedX - 1, checkedY, checkedZ)) < checkedNewLight) {
								this.lightUpdateBlockList[toCheckCount++] = checkedX - 1 - posX + 32 + (checkedY - posY + 32 << 6) + (checkedZ - posZ + 32 << 12);
							}

							if (this.getLightFor(lightType, checkedPos.setPos(checkedX + 1, checkedY, checkedZ)) < checkedNewLight) {
								this.lightUpdateBlockList[toCheckCount++] = checkedX + 1 - posX + 32 + (checkedY - posY + 32 << 6) + (checkedZ - posZ + 32 << 12);
							}

							if (this.getLightFor(lightType, checkedPos.setPos(checkedX, checkedY - 1, checkedZ)) < checkedNewLight) {
								this.lightUpdateBlockList[toCheckCount++] = checkedX - posX + 32 + (checkedY - 1 - posY + 32 << 6) + (checkedZ - posZ + 32 << 12);
							}

							if (this.getLightFor(lightType, checkedPos.setPos(checkedX, checkedY + 1, checkedZ)) < checkedNewLight) {
								this.lightUpdateBlockList[toCheckCount++] = checkedX - posX + 32 + (checkedY + 1 - posY + 32 << 6) + (checkedZ - posZ + 32 << 12);
							}

							if (this.getLightFor(lightType, checkedPos.setPos(checkedX, checkedY, checkedZ - 1)) < checkedNewLight) {
								this.lightUpdateBlockList[toCheckCount++] = checkedX - posX + 32 + (checkedY - posY + 32 << 6) + (checkedZ - 1 - posZ + 32 << 12);
							}

							if (this.getLightFor(lightType, checkedPos.setPos(checkedX, checkedY, checkedZ + 1)) < checkedNewLight) {
								this.lightUpdateBlockList[toCheckCount++] = checkedX - posX + 32 + (checkedY - posY + 32 << 6) + (checkedZ + 1 - posZ + 32 << 12);
							}
						}
					}
				}
			}

			checkedPos.release();

			this.theProfiler.endSection();
			return true;
		}
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return null;
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return false;
	}
}
