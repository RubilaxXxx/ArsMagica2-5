package am2.blocks.tileentities;

import am2.ObeliskFuelHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.blocks.MultiblockStructureDefinition.StructureGroup;
import am2.api.power.IPowerSource;
import am2.api.power.PowerTypes;
import am2.blocks.BlocksCommonProxy;
import am2.buffs.BuffEffectManaRegen;
import am2.buffs.BuffList;
import am2.multiblock.IMultiblockStructureController;
import am2.network.AMDataReader;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.utility.InventoryUtilities;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityObelisk extends TileEntityPowerSources implements IMultiblockStructureController, IInventory, IPowerSource{
//	protected static int pillarBlockID = 98; //stone brick
//	protected static int pillarBlockMeta = 3; //arcane texture
	private ItemStack[] inventory;

	public boolean active = false;
	public float offsetY = 0;
	public int burnTimeRemaining = 0;
	public int maxBurnTime = 1;
	private static final byte PK_BURNTIME_CHANGE = 1;
	private static final int GROUP_CHISELED_STONE = 0;
	//TAGS
	private static final String TAG_POWERAMOUNT = "poweramount";
	private static final String TAG_BURNTIMEREMAINING = "burntimeremaining";
	private static final String TAG_BURNINVENTORY = "burninventory";


	public TileEntityObelisk(){
		this(5000);
		inventory = new ItemStack[this.getSizeInventory()];
	}



	public TileEntityObelisk(int capacity){
		super(capacity,PowerTypes.NEUTRAL);
		surroundingCheckTicks = 0;

		structure = new MultiblockStructureDefinition("obelisk_structure");

		pillars = structure.createGroup("pillars", 2);
		caps = new HashMap<StructureGroup, Float>();
		StructureGroup chiseled = structure.createGroup("caps_chiseled_stone", 4);
		caps.put(chiseled, 1.35f);

		structure.addAllowedBlock(0, 0, 0, BlocksCommonProxy.obelisk);

		structure.addAllowedBlock(pillars, -2, 0, -2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(pillars, -2, 1, -2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(chiseled, -2, 2, -2, Blocks.stonebrick, 3);

		structure.addAllowedBlock(pillars, 2, 0, -2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(pillars, 2, 1, -2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(chiseled, 2, 2, -2, Blocks.stonebrick, 3);

		structure.addAllowedBlock(pillars, -2, 0, 2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(pillars, -2, 1, 2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(chiseled, -2, 2, 2, Blocks.stonebrick, 3);

		structure.addAllowedBlock(pillars, 2, 0, 2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(pillars, 2, 1, 2, Blocks.stonebrick, 0);
		structure.addAllowedBlock(chiseled, 2, 2, 2, Blocks.stonebrick, 3);

		wizardChalkCircle = structure.addWizChalkGroupToStructure(1);
	}

	public boolean isActive(){
		return active;
	}

//	public boolean isHighPowerActive(){
//		return burnTimeRemaining > 200;
//	}

	@SideOnly(Side.CLIENT)
	public int getCookProgressScaled(int par1){
		return burnTimeRemaining * par1 / maxBurnTime;
	}

	private void sendCookUpdateToClients(){
		if (!worldObj.isRemote){
			AMNetHandler.INSTANCE.sendObeliskUpdate(this, new AMDataWriter().add(PK_BURNTIME_CHANGE).add(this.burnTimeRemaining).generate());
		}
	}

	public void handlePacket(byte[] data){
		AMDataReader rdr = new AMDataReader(data);
		if (rdr.ID == PK_BURNTIME_CHANGE)
			this.burnTimeRemaining = rdr.getInt();
	}
	protected void checkNearbyBlockState(){
		ArrayList<StructureGroup> groups = structure.getMatchedGroups(7, worldObj, xCoord, yCoord, zCoord);

		float capsLevel = 1;
		boolean pillarsFound = false;
		boolean wizChalkFound = false;

		for (StructureGroup group : groups){
			if (group == pillars)
				pillarsFound = true;
			else if (group == wizardChalkCircle)
				wizChalkFound = true;

			for (StructureGroup cap : caps.keySet()){
				if (group == cap){
					capsLevel = caps.get(cap);
				}
			}
		}

		powerMultiplier = 1;

		if (wizChalkFound)
			powerMultiplier = 1.25f;

		if (pillarsFound)
			powerMultiplier *= capsLevel;
	}

	@Override
	public void updateEntity(){
		surroundingCheckTicks++;
		super.updateEntity();
		active = false;
		if(surroundingCheckTicks % 100 == 0){
			checkNearbyBlockState();
			surroundingCheckTicks = 1;
		}
		if(burnTimeRemaining > 0){
			--burnTimeRemaining;
			active = true;
		}
		if (!worldObj.isRemote){

			boolean isfull = this.getCharge() / this.getCapacity() == 1;
			if (burnTimeRemaining != 0 || this.inventory[0] != null){
				if (burnTimeRemaining == 0 && !isfull){
					maxBurnTime = burnTimeRemaining = ObeliskFuelHelper.instance.getFuelBurnTime(inventory[0]);
					if (burnTimeRemaining > 0){
						active = true;
						if (this.inventory[0] != null){
								--this.inventory[0].stackSize;
						if (this.inventory[0].stackSize == 0){
							this.inventory[0] = this.inventory[0].getItem().getContainerItem(this.inventory[0]);
						}
						}
					}
				}
				if (burnTimeRemaining > 0 && burnTimeRemaining % 20 == 0){
					active = true;
					this.setCharge((int)(powerBase * powerMultiplier));
					sendCookUpdateToClients();
				}
			}
		}
		if(isActive()){
			markDirty();
			if (!worldObj.isRemote && this.getCharge() >= this.capacity * 0.1f){
				List<EntityPlayer> nearbyPlayers = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.xCoord - 2, this.yCoord, this.zCoord - 2, this.xCoord + 2, this.yCoord + 3, this.zCoord + 2));
				for (EntityPlayer p : nearbyPlayers){
					if (p.isPotionActive(BuffList.manaRegen.id)) continue;
					p.addPotionEffect(new BuffEffectManaRegen(600, 2));
				}
			}
		}


	}
	//		else{
//			surroundingCheckTicks = 1;
//		lastOffsetY = offsetY;
//			offsetY = (float)Math.max(Math.sin(worldObj.getTotalWorldTime() / 20f) / 5, 0.25f);
//		}







	@Override
	public MultiblockStructureDefinition getDefinition(){
		return structure;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger(TAG_BURNTIMEREMAINING, burnTimeRemaining);


		if (inventory != null){
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < inventory.length; i++){
				if (inventory[i] != null){
					String tag = String.format("ArrayIndex", i);
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte(tag, (byte)i);
					inventory[i].writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag(TAG_BURNINVENTORY, nbttaglist);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		burnTimeRemaining = nbttagcompound.getInteger(TAG_BURNTIMEREMAINING);


		if (nbttagcompound.hasKey(TAG_BURNINVENTORY)){
			NBTTagList nbttaglist = nbttagcompound.getTagList(TAG_BURNINVENTORY, Constants.NBT.TAG_COMPOUND);
			inventory = new ItemStack[getSizeInventory()];
			for (int i = 0; i < nbttaglist.tagCount(); i++){
				String tag = String.format("ArrayIndex", i);
				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
				byte byte0 = nbttagcompound1.getByte(tag);
				if (byte0 >= 0 && byte0 < inventory.length){
					inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				}
			}
		}
	}

	@Override
	public int getChargeRate(){
		return 0;
	}

	@Override
	public boolean canSendPower(PowerTypes type){
		return type == PowerTypes.NEUTRAL;
	}

	@Override
	public PowerTypes[] getValidPowerTypes(){
		return new PowerTypes[]{PowerTypes.NEUTRAL};
	}

	public int getCharge(){
		return this.poweramount;
	}


	//------------------------------------------
	//--   INVENTORY  HANDLER          ---------
	//------------------------------------------
	@Override
	public int getSizeInventory(){
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < 0 || i >= this.getSizeInventory())
			return null;
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (inventory[i] != null){
			if (inventory[i].stackSize <= j){
				ItemStack itemstack = inventory[i];
				inventory[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = inventory[i].splitStack(j);
			if (inventory[i].stackSize == 0){
				inventory[i] = null;
			}
			return itemstack1;
		}else{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i){
		if (inventory[i] != null){
			ItemStack itemstack = inventory[i];
			inventory[i] = null;
			return itemstack;
		}else{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		inventory[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()){
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName(){
		return "obelisk";
	}

	@Override
	public boolean hasCustomInventoryName(){
		return false;
	}


	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer){
		if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this){
			return false;
		}
		return entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openInventory(){
	}

	@Override
	public void closeInventory(){
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return ObeliskFuelHelper.instance.getFuelBurnTime(itemstack) > 0;
	}


}
