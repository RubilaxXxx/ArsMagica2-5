package am2.blocks.tileentities;

import am2.api.power.PowerTypes;
import am2.entities.EntityFlicker;
import am2.power.PowerNodeRegistry;

import static am2.api.power.PowerTypes.NONE;

public class TileEntityFlickerLure extends TileEntityManaConsumer{

	public TileEntityFlickerLure(){
		super(200, new PowerTypes[]{NONE});
	}




	@Override
	public void updateEntity(){
		super.updateEntity();

		if (worldObj.isRemote)
			return;

		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)){
			if (worldObj.rand.nextDouble() < 0.005f && PowerNodeRegistry.instance.checkPower(this, 100)){
				EntityFlicker flicker = new EntityFlicker(worldObj);
				flicker.setPosition(xCoord + 0.5f, yCoord + 1.5f, zCoord + 0.5f);
				worldObj.spawnEntityInWorld(flicker);
				PowerNodeRegistry.instance.consumePower(this, PowerNodeRegistry.instance.getHighestPowerType(this), 100);
			}
		}
	}
}
