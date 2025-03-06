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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
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
	protected String TAG_POWERAMOUNT = "poweramount";


	public TileEntityPowerSources(int capacity, PowerTypes type){
		this.capacity = capacity;
		this.type = type;

	}
	@Override
	public boolean bindTo(World world, int x, int y, int z, EntityPlayer player){
		return false;
	}
	public void GenerateStructureData(){}

	@Override
	public boolean unbind(World world, int x, int y, int z){
		return false;
	}

	@Override
	public int getCapacity(){
		return this.capacity;
	}

	@Override
	public int getCharge(){
		return this.poweramount;
	}

	@Override
	public boolean canSendPower(PowerTypes type){
		return type == this.type;
	}
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger(TAG_POWERAMOUNT, getCharge());

	}
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		setCharge(nbttagcompound.getInteger(TAG_POWERAMOUNT));
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
	public PowerTypes[] getValidPowerTypes(){
		return new PowerTypes[0];
	}

	@Override
	public float particleOffset(int axis){
		return 0;
	}
	@Override
	public Packet getDescriptionPacket(){
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -888, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet){
		readFromNBT(packet.func_148857_g());
	}


	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 0.3, zCoord + 2);
	}

	@Override
	public MultiblockStructureDefinition getDefinition(){
		return this.structure;
	}

	@Override
	public void setCharge(int amount){
		 this.poweramount = Math.max(0, Math.min(getCharge() + amount, getCapacity()));
	}
}
