package am2.blocks.tileentities;

import am2.api.power.IBindable;
import am2.api.power.IManaPower;
import am2.api.power.PowerTypes;
import am2.power.PowerNodeRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public abstract class TileEntityAMManaPower extends TileEntity implements IManaPower, IBindable{
	protected int capacity;
	protected boolean canRequestPower = true;
	protected TileEntity Binded;


	private static final int REQUEST_INTERVAL = 20;

	public TileEntityAMManaPower(int capacity){
		this.capacity = capacity;
	}

	protected void setNoPowerRequests(){
		canRequestPower = false;
	}

	protected void setPowerRequests(){
		canRequestPower = true;
	}

	/***
	 * Whether the tile entity can provide power.
	 */
	@Override
	public boolean canSendPower(PowerTypes type){
		return false;
	}

	public boolean unbind(World world, int x, int y, int z){
		if(GetBinded() == null){
			return false;
		}
		this.Binded = null;
		return true;
	}
	public TileEntity GetBinded(){
		return this.Binded;
	}
	public boolean bindTo(World world, int x, int y, int z, EntityPlayer player){
		TileEntity te = world.getTileEntity(x, y, z);
		if(te.isInvalid()){
			player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.error")));
			return false;
		}
		this.Binded = te;
		return true;
	}
	@Override
	public void invalidate(){
		PowerNodeRegistry.instance.remove(this);
		super.invalidate();
	}
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		invalidate();
	}

	@Override
	public void updateEntity(){
		if(!PowerNodeRegistry.instance.IsNodeInit(this))
			PowerNodeRegistry.instance.add(this);
	}

	public int getRequestInterval(){
		return REQUEST_INTERVAL;
	}

	@Override
	public float particleOffset(int axis){
		return 0.5f;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
	}

	@Override
	public int getCapacity(){
		return this.capacity;
	}

	private void setPower(PowerTypes type, int amount){
		PowerNodeRegistry.instance.setPower(this, type, amount);
	}

	@Override
	public PowerTypes[] getValidPowerTypes(){
		return PowerTypes.all();
	}

	@Override
	public boolean canReceivePower(){
		return this.canRequestPower;
	}

	@Override
	public boolean isSource(){
		return false;
	}
}
