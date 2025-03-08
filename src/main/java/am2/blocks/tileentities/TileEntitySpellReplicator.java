package am2.blocks.tileentities;

import am2.AMCore;
import am2.api.power.PowerTypes;
import am2.items.SpellBase;
import am2.particles.AMParticle;
import am2.particles.ParticleHoldPosition;
import am2.power.PowerNodeRegistry;
import am2.spell.SpellUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;

import java.util.List;

import static am2.api.power.PowerTypes.LIGHT;

public class TileEntitySpellReplicator extends TileEntityManaConsumer implements IInventory{
	public float yetToConsume;
	private ItemStack[] inventory;
	@SideOnly(Side.CLIENT)
	AMParticle particle;

	public TileEntitySpellReplicator(){
		super(30000, new PowerTypes[]{LIGHT});
		inventory = new ItemStack[getSizeInventory()];
		yetToConsume = -1;
	}

	@Override
	public void updateEntity(){
		super.updateEntity();
		if (worldObj.isRemote){
			for (int i = 0; i < AMCore.config.getGFXLevel(); ++i){
				particle = (AMParticle)AMCore.proxy.particleManager.spawn(worldObj, isWorking() ? "symbols" : "ember", xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f);
				if (isWorking()) particle.addRandomOffset(0.6, 0.6, 0.6);
				else particle.addRandomOffset(0.4, 0.4, 0.4);
				if (particle != null){
					particle.setMaxAge(20);
					particle.setRGBColorF(0.05f, 0.8f, 0.65f);
					particle.setParticleScale(0.1f);
					particle.AddParticleController(new ParticleHoldPosition(particle, 1000, 1, false));
				}
			}
		}

		if (this.inventory[0] != null && yetToConsume == -1){
			yetToConsume = getTotalReplicateCost();
		} else if (yetToConsume > 0){
			if (!worldObj.isRemote){
				float availablePower = PowerNodeRegistry.instance.getPower(this, LIGHT);
				PowerNodeRegistry.instance.consumePower(this, LIGHT, Math.min(yetToConsume, Math.min(availablePower, 100)));
				yetToConsume -= Math.min(yetToConsume, Math.min(availablePower, 100));
			}
		} else if (this.inventory[0] != null && yetToConsume == 0){
			if (!worldObj.isRemote){
				yetToConsume = -1;
				ItemStack newItem = new ItemStack(this.inventory[0].getItem(), 2, this.inventory[0].getItemDamage());
				newItem.setTagCompound(this.inventory[0].getTagCompound());
				EntityItem entityitem = new EntityItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, newItem);
				float f3 = 0.05F;
				entityitem.motionX = (float)worldObj.rand.nextGaussian() * f3;
				entityitem.motionY = (float)worldObj.rand.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float)worldObj.rand.nextGaussian() * f3;
				worldObj.spawnEntityInWorld(entityitem);
				this.setInventorySlotContents(0, null);
				List<EntityPlayerMP> players = this.worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1).expand(256, 256, 256));
				for (EntityPlayerMP player : players){
					player.playerNetServerHandler.sendPacket(getDescriptionPacket());
				}
			}
		}
	}

	private boolean isWorking(){
		return this.inventory[0] != null && yetToConsume > 0;
	}

	private float getTotalReplicateCost(){
		if (this.inventory[0] != null && this.inventory[0].getItem() instanceof SpellBase) return SpellUtils.instance.getSpellRequirements(this.inventory[0], this.worldObj.getClosestPlayer(this.xCoord, this.yCoord, this.zCoord, -1)).manaCost * 10;
		return -1;
	}

	@Override
	public int getSizeInventory(){
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < 0 || i >= getSizeInventory()) return null;
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
		return "Spell Replicator";
	}

	@Override
	public boolean hasCustomInventoryName(){
		return false;
	}

	@Override
	public int getInventoryStackLimit(){
		return 1;
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
		return false;
	}


	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("ReplicatorInventory", Constants.NBT.TAG_COMPOUND);
		inventory = new ItemStack[getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < inventory.length){
				inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		this.yetToConsume = nbttagcompound.getFloat("YetToConsume");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);

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

		nbttagcompound.setTag("ReplicatorInventory", nbttaglist);
		nbttagcompound.setFloat("YetToConsume", yetToConsume);
	}

	@Override
	public Packet getDescriptionPacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, worldObj.getBlockMetadata(xCoord, yCoord, zCoord), compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.func_148857_g());
	}

}
