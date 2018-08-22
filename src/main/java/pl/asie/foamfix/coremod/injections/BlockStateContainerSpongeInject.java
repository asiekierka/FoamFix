package pl.asie.foamfix.coremod.injections;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.BlockStateContainer;

public class BlockStateContainerSpongeInject extends BlockStateContainer {
	@Override
	public int getSerializedSize() {
		int len = this.storage.getBackingLongArray().length;
		return 1 + this.palette.getSerializedSize() + PacketBuffer.getVarIntSize(len) + len * 8;
	}
}
