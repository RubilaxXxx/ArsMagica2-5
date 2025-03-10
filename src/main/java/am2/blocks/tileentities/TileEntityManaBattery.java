package am2.blocks.tileentities;

import am2.api.power.PowerTypes;
import am2.blocks.BlocksCommonProxy;
import am2.power.PowerNodeRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityManaBattery extends TileEntityAMPower{

	private boolean active;

	private PowerTypes outputPowerType = PowerTypes.NONE;
	private int tickCounter = 0;

	public TileEntityManaBattery(){
		super(250000);
		active = false;
	}

	public PowerTypes getPowerType(){
		return outputPowerType;
	}

	public void setPowerType(PowerTypes type, boolean forceSubNodes){
		this.outputPowerType = type;
		if (worldObj != null && worldObj.isRemote)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void setActive(boolean active){
		this.active = active;
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return true;
	}

	@Override
	public void updateEntity(){
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)){
			this.setPowerRequests();
		}else{
			this.setNoPowerRequests();
		}

		if (!this.worldObj.isRemote){
			PowerTypes highest = PowerNodeRegistry.For(worldObj).getHighestPowerType(this);
			float amt = PowerNodeRegistry.For(worldObj).getPower(this, highest);
			if (amt > 0){
				this.outputPowerType = highest;
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, highest.ID(), 2);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}else{
				this.outputPowerType = PowerTypes.NONE;
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		tickCounter++;
		if (tickCounter % 600 == 0){
			worldObj.notifyBlockChange(xCoord, yCoord, zCoord, BlocksCommonProxy.manaBattery);
			tickCounter = 0;
		}

		super.updateEntity();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("isActive", active);
		nbttagcompound.setInteger("outputType", outputPowerType.ID());
		nbttagcompound.setFloat("power_charge", PowerNodeRegistry.For(this.worldObj).getPower(this,this.getPowerType()));

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		active = nbttagcompound.getBoolean("isActive");
		if(nbttagcompound.hasKey("outputType"))
			outputPowerType = PowerTypes.getByID(nbttagcompound.getInteger("outputType"));
		if(nbttagcompound.hasKey("power_charge"))
			PowerNodeRegistry.For(this.worldObj).setPower(this,outputPowerType,nbttagcompound.getFloat("power_charge"));
	}

	@Override
	public Packet getDescriptionPacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, worldObj.getBlockMetadata(xCoord, yCoord, zCoord), compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.func_148857_g());
	}

	@Override
	public int getChargeRate(){
		return 1000;
	}

	@Override
	public PowerTypes[] getValidPowerTypes(){
		if (this.outputPowerType == PowerTypes.NONE)
			return PowerTypes.all();
		return new PowerTypes[]{this.outputPowerType};
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}
}
