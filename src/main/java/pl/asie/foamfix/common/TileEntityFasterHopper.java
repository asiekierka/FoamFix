/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
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
