package am2.items;

import am2.AMCore;
import am2.guis.ArsMagicaGuiIdList;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemEssenceBag extends ArsMagicaItem{

	public ItemEssenceBag(){
		super();
		setMaxStackSize(1);
		setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack,
							   EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		par3List.add(StatCollector.translateToLocal("am2.tooltip.rupees"));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer entityplayer){
		if (!world.isRemote){
			FMLNetworkHandler.openGui(entityplayer, AMCore.instance, ArsMagicaGuiIdList.GUI_ESSENCE_BAG, world, (int)entityplayer.posX, (int)entityplayer.posY, (int)entityplayer.posZ);
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
		ItemStack[] stackList = new ItemStack[12];
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

	public InventoryEssenceBag ConvertToInventory(ItemStack essenceBagStack){
		InventoryEssenceBag ieb = new InventoryEssenceBag();
		ieb.SetInventoryContents(getMyInventory(essenceBagStack));
		return ieb;
	}
}
