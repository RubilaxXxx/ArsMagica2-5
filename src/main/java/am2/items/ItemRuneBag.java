package am2.items;

import am2.AMCore;
import am2.guis.ArsMagicaGuiIdList;
import am2.texture.ResourceManager;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class ItemRuneBag extends Item{

	public ItemRuneBag(){
		super();
		this.maxStackSize = 1;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer entityplayer){
		if (!world.isRemote){
			FMLNetworkHandler.openGui(entityplayer, AMCore.instance, ArsMagicaGuiIdList.GUI_RUNE_BAG, world, (int)entityplayer.posX, (int)entityplayer.posY, (int)entityplayer.posZ);
		}
		return stack;
	}

	private ItemStack[] getMyInventory(ItemStack itemStack){
		return ReadFromStackTagCompound(itemStack);
	}

	public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values){
		NBTTagList stacks = new NBTTagList();
		for(int slot = 0; slot < values.length; ++slot){
			if(values[slot] != null){
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot",(byte) slot);
				values[slot].writeToNBT(item);
				stacks.appendTag(item);
			}
		}
		itemStack.setTagInfo("Inventory", stacks);

	}

	public ItemStack[] ReadFromStackTagCompound(ItemStack item){
		ItemStack[] stackList = new ItemStack[16];
		if(item.hasTagCompound()) {
			NBTTagList var2 = item.stackTagCompound.getTagList("Inventory", 10);

			for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
				NBTTagCompound var4 = var2.getCompoundTagAt(var3);
				int var5 = var4.getByte("Slot") & 255;
				if(var5 >= 0 && var5 < stackList.length) {
					stackList[var5] = ItemStack.loadItemStackFromNBT(var4);
				}
			}
		}
		return stackList;
	}

	public InventoryRuneBag ConvertToInventory(ItemStack runeBagStack){
		InventoryRuneBag irb = new InventoryRuneBag();
		irb.SetInventoryContents(getMyInventory(runeBagStack));
		return irb;
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = ResourceManager.RegisterTexture("rune_bag", par1IconRegister);
	}

}
