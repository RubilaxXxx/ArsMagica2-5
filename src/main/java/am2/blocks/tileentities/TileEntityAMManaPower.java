package am2.blocks.tileentities;

import am2.api.power.IBindable;
import am2.api.power.IManaPower;
import am2.api.power.IWrenchable;
import am2.api.power.PowerTypes;
import am2.power.PowerNodeRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import static am2.network.AMNetHandler.sendTiledatatoClient;

public  class TileEntityAMManaPower extends TileEntity implements IManaPower, IBindable, IWrenchable{
	protected int capacity;
	protected boolean canRequestPower = true;
	protected TileEntity Binded;
	public static final String TAG_OUTPUTTYPE = "outputType";
	public static final String TAG_POWERAMOUNT = "PowerAmount";
	protected int poweramount = 0;
	protected PowerTypes outputPowerType = PowerTypes.NONE;

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

	@Override
	public boolean canRelayPower(PowerTypes type){
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
		if (nbttagcompound.hasKey(TAG_OUTPUTTYPE) && nbttagcompound.hasKey(TAG_POWERAMOUNT)){
			outputPowerType = PowerTypes.getByID(nbttagcompound.getInteger(TAG_OUTPUTTYPE));
			this.setPower(outputPowerType, nbttagcompound.getInteger(TAG_POWERAMOUNT));
		}
	}


	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger(TAG_OUTPUTTYPE, outputPowerType.ID());
		nbttagcompound.setInteger(TAG_POWERAMOUNT, getCharge());
	}

	@Override
	public int getCapacity(){
		return this.capacity;
	}

	@Override
	public int getCharge(){
		return poweramount;
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

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet){
		readFromNBT(packet.func_148857_g());
	}

	@Override
	public void WrenchClick(EntityPlayer player, ItemStack stack){
		if(player == null) return;
		if(!worldObj.isRemote){
			if(player instanceof EntityPlayerMP){
				sendTiledatatoClient(this, (EntityPlayerMP)player);
				player.addChatMessage(new ChatComponentText(String.format(StatCollector.translateToLocal("am2.tooltip.det_eth"),
						outputPowerType.chatColor(), outputPowerType.name(), getCharge())));
			}
		}
	}

	public void setCharge(PowerTypes type, int amount){
		if(outputPowerType != PowerTypes.NONE && outputPowerType != type){
			return;
		}
		else if(outputPowerType == PowerTypes.NONE){
			outputPowerType = type;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, type.ID(), 2);
			setCharge(amount);
			this.markDirty();
			return;
		}
		setCharge(amount);
		this.markDirty();
	}
	public void setCharge(int amount){
		this.poweramount = Math.max(0, Math.min(getCharge() + amount, getCapacity()));
	}


	private void setPower(int power){
		this.poweramount = Math.min(power, getCapacity());
	}
	private void setPower(PowerTypes type, int power){
		if(outputPowerType != PowerTypes.NONE && outputPowerType != type){
			return;
		}
		else if(outputPowerType == PowerTypes.NONE){
			outputPowerType = type;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, type.ID(), 2);
			setPower(power);
			this.markDirty();
			return;
		}
		setPower(power);
		this.markDirty();
	}

}
