package am2.blocks.tileentities;

import am2.api.power.IManaPower;
import am2.api.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;


public class TileEntityManaBattery extends TileEntityManaStorage{
	private static final String ACTIVE = "active";
	private int tick = 0;



	public TileEntityManaBattery(){
		super(250000);

	}

	public boolean IsActive(){
		return this.canReceivePower();
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
			if (this.IsActive() && 20 <= tick){
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



	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.getBoolean(ACTIVE)){
			this.setPowerRequests();
		}else{
			this.setNoPowerRequests();
		}

	}


	//------------------------------------------
	//---           SETTERS               ------
	//------------------------------------------




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
