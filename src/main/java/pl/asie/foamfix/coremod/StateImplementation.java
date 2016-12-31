package pl.asie.foamfix.coremod;

import com.google.common.collect.*;
import com.sun.corba.se.impl.util.IdentityHashtable;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StateImplementation extends BlockStateContainer.StateImplementation implements IFoamyBlockState {
	private final Object owner;
	protected ImmutableMap < IProperty<?>, Comparable<? >> properties;
	protected int value;

	public StateImplementation(Object owner, Block blockIn, ImmutableMap < IProperty<?>, Comparable<? >> propertiesIn) {
		super(blockIn, propertiesIn);
		this.owner = owner;
		this.properties = propertiesIn;
		PropertyValueMapper.getPropertiesOrdered(blockIn, propertiesIn.keySet());
	}

	@Override
	public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value)
	{
		Comparable<?> comparable = (Comparable)this.properties.get(property);

		if (comparable == null)
		{
			throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.getBlock().getBlockState());
		}
		else if (comparable == value)
		{
			return this;
		}
		else
		{
			IBlockState iblockstate = PropertyValueMapper.withProperty(this, this.value, property, value);

			if (iblockstate == null)
			{
				throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(this.getBlock()) + ", it is not an allowed value");
			}
			else
			{
				return iblockstate;
			}
		}
	}

	@Override
	public void buildPropertyValueTable(Map <Map< IProperty<?>, Comparable<? >>, BlockStateContainer.StateImplementation > map) {
		this.value = PropertyValueMapper.generateValue(this);
	}

	@Override
	public Object getFoamyOwner() {
		return owner;
	}
}
