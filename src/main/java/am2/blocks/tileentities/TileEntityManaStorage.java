package am2.blocks.tileentities;

import am2.api.power.IBindable;
import am2.api.power.IManaPower;
import am2.api.power.IWrenchable;
import am2.api.power.PowerTypes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityManaStorage extends TileEntity implements IManaPower, IBindable, IWrenchable{
	protected int capacity;
	protected boolean canRequestPower = true;
	protected TileEntity Binded;
	public static final String TAG_OUTPUTTYPE = "outputType";
	public static final String TAG_POWERAMOUNT = "PowerAmount";
	protected int poweramount = 0;
	protected PowerTypes outputPowerType = PowerTypes.NONE;

	private static final int REQUEST_INTERVAL = 20;

	protected void setNoPowerRequests(){
		canRequestPower = false;
	}

	protected void setPowerRequests(){
		canRequestPower = true;
	}

	public TileEntityManaStorage(int capacity){
		this.capacity = capacity;
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
	public TileEntity GetBinded(){
		return this.Binded;
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
	public void setCharge(PowerTypes type, int amount){

	}

	@Override
	public void setCharge(int amount){

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
		return false;
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
	public void WrenchClick(EntityPlayer player, ItemStack stack){

	}
}
