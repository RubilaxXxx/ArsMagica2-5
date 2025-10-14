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

	public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values) {
		if (itemStack.stackTagCompound == null) {
			itemStack.stackTagCompound = new NBTTagCompound();
		}
		for (int i = 0; i < values.length; ++i) {
			ItemStack stack = values[i];
			if (stack == null) {
				itemStack.stackTagCompound.removeTag("runebagstacksize" + i);
				itemStack.stackTagCompound.removeTag("runebagmeta" + i);
			} else {
				itemStack.stackTagCompound.setInteger("runebagstacksize" + i, stack.stackSize);
				itemStack.stackTagCompound.setInteger("runebagmeta" + i, stack.getItemDamage());
			}
		}
	}
	public ItemStack[] ReadFromStackTagCompound(ItemStack itemStack) {
		if (itemStack.stackTagCompound == null) {
			return new ItemStack[InventoryRuneBag.inventorySize];
		}
		ItemStack[] items = new ItemStack[InventoryRuneBag.inventorySize];
		for (int i = 0; i < items.length; ++i) {
			if (!itemStack.stackTagCompound.hasKey("runebagmeta" + i)
					|| itemStack.stackTagCompound.getInteger("runebagmeta" + i) == -1) {
				items[i] = null;
				continue;
			}
			int stacksize = itemStack.stackTagCompound.getInteger("runebagstacksize" + i);
			int meta = itemStack.stackTagCompound.getInteger("runebagmeta" + i);
			items[i] = new ItemStack(ItemsCommonProxy.rune, stacksize, meta);
		}
		return items;
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
