package pl.asie.foamfix.common;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityFasterHopper extends TileEntityHopper {
    private boolean empty, full;
    private boolean dirtyEmpty, dirtyFull;

    @Override
    public void validate() {
        super.validate();
        dirtyEmpty = true;
        dirtyFull = true;
    }

    public void updateFlagEmpty() {
        empty = true;

        for (ItemStack stack : getItems()) {
            if (!stack.isEmpty()) {
                empty = false;
                return;
            }
        }
    }

    public void updateFlagFull() {
        full = true;

        for (ItemStack stack : getItems()) {
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                full = false;
                return;
            }
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = super.removeStackFromSlot(index);
        if (!stack.isEmpty()) {
            dirtyEmpty = true;
            full = false; // Something was taken out, so it won't be full anymore
        }
        return stack;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = super.decrStackSize(index, count);
        if (!stack.isEmpty()) {
            dirtyEmpty = true;
            full = false; // Something was taken out, so it won't be full anymore
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        super.setInventorySlotContents(index, stack);
        dirtyEmpty = true;
        dirtyFull = true;
    }

    @Override
    protected boolean isInventoryEmpty() {
        if (dirtyEmpty) {
            updateFlagEmpty();
            dirtyEmpty = false;
        }
        return empty;
    }

    @Override
    protected boolean isFull() {
        if (dirtyFull) {
            updateFlagFull();
            dirtyFull = false;
        }
        return full;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
