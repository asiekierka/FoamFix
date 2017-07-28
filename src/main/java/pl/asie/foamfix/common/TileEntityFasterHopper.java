package pl.asie.foamfix.common;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;

public class TileEntityFasterHopper extends TileEntityHopper {
    private boolean empty, full;
    private boolean dirtyEmpty = true, dirtyFull = true;

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
        }
        return stack;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = super.decrStackSize(index, count);
        if (!stack.isEmpty()) {
            dirtyEmpty = true;
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
}
