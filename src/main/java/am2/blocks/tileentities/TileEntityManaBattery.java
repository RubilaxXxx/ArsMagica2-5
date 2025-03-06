package am2.blocks.tileentities;

import am2.api.power.IManaPower;
import am2.api.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;


public class TileEntityManaBattery extends TileEntityAMManaPower{
	private static final String ACTIVE = "active";
	public static final String TAG_OUTPUTTYPE = "outputType";
	public static final String TAG_POWERAMOUNT = "PowerAmount";


	private PowerTypes outputPowerType = PowerTypes.NONE;
	private int tick = 0;
	private int poweramount = 0;


	public TileEntityManaBattery(){
		super(250000);

	}

	public boolean IsActive(){
		return this.canReceivePower();
	}

	@Override
	public boolean canSendPower(PowerTypes type){
		return true;
	}

	@Override
	public void updateEntity(){
		super.updateEntity();
		tick++;
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)){
			this.setNoPowerRequests();
		}else{
			this.setPowerRequests();
		}
		if(!worldObj.isRemote){
			if (this.canRequestPower && getRequestInterval() <= tick){
				tick = 0;
				if (GetBinded() != null && ((IManaPower)GetBinded()).getCharge() > 0){
					return;
				}
			}
			if (getCharge() == 0){
				this.outputPowerType = PowerTypes.NONE;
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
				this.markDirty();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean(ACTIVE, this.IsActive());
		nbttagcompound.setInteger(TAG_OUTPUTTYPE, outputPowerType.ID());
		nbttagcompound.setInteger(TAG_POWERAMOUNT, getCharge());

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.getBoolean(ACTIVE)){
			this.setPowerRequests();
		}else{
			this.setNoPowerRequests();
		}
		if (nbttagcompound.hasKey(TAG_OUTPUTTYPE) && nbttagcompound.hasKey(TAG_POWERAMOUNT)){
			outputPowerType = PowerTypes.getByID(nbttagcompound.getInteger(TAG_OUTPUTTYPE));
			this.setPower(outputPowerType, nbttagcompound.getInteger(TAG_POWERAMOUNT));
		}
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}


	//------------------------------------------
	//---           SETTERS               ------
	//------------------------------------------
	private void setCharge(int amount){
		this.poweramount = Math.max(0, Math.min(getCharge() + amount, getCapacity()));

	}
	public void setPower(PowerTypes type, int amount){
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

	public int getCharge(){
		return this.poweramount;
	}



	@Override
	public PowerTypes[] getValidPowerTypes(){
		if (this.outputPowerType == PowerTypes.NONE)
			return PowerTypes.all();
		return new PowerTypes[]{this.outputPowerType};
	}

	public PowerTypes getPowerType(){
		return this.outputPowerType;
	}


}
/*
	public void setPowerType(PowerTypes type){
		this.outputPowerType = type;
		if (worldObj != null && worldObj.isRemote)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
*/
