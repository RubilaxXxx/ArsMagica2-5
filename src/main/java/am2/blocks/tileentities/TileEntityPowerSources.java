package am2.blocks.tileentities;

import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.power.IBindable;
import am2.api.power.IManaPower;
import am2.api.power.IPowerSource;
import am2.api.power.PowerTypes;
import am2.multiblock.IMultiblockStructureController;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.HashMap;

public class TileEntityPowerSources extends TileEntity implements IManaPower, IBindable, IMultiblockStructureController, IPowerSource{
	protected PowerTypes type;
	protected int capacity;
	protected int surroundingCheckTicks;
	protected int poweramount;
	protected int powerBase;
	protected float powerMultiplier;
	protected MultiblockStructureDefinition structure;
	protected MultiblockStructureDefinition.StructureGroup wizardChalkCircle;
	protected MultiblockStructureDefinition.StructureGroup pillars;
	protected HashMap<MultiblockStructureDefinition.StructureGroup, Float> caps;


	public TileEntityPowerSources(int capacity, PowerTypes type){
		this.capacity = capacity;
		this.type = type;

	}
	@Override
	public boolean bindTo(World world, int x, int y, int z, EntityPlayer player){
		return false;
	}

	@Override
	public boolean unbind(World world, int x, int y, int z){
		return false;
	}

	@Override
	public int getCapacity(){
		return capacity;
	}

	@Override
	public int getCharge(){
		return 0;
	}

	@Override
	public boolean canSendPower(PowerTypes type){
		return false;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public boolean canReceivePower(){
		return false;
	}

	@Override
	public boolean isSource(){
		return true;
	}

	@Override
	public int getChargeRate(){
		return 0;
	}

	@Override
	public PowerTypes[] getValidPowerTypes(){
		return new PowerTypes[0];
	}

	@Override
	public float particleOffset(int axis){
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 0.3, zCoord + 2);
	}

	@Override
	public MultiblockStructureDefinition getDefinition(){
		return structure;
	}

	@Override
	public void setCharge(int amount){
		 poweramount = Math.max(0, Math.min(getCharge() + amount, getCapacity()));
	}
}
