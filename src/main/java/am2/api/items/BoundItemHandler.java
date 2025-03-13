package am2.api.items;

import am2.items.ItemsCommonProxy;
import am2.spell.SpellUtils;
import am2.utility.InventoryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BoundItemHandler extends Item{

	public static void UnbindItem(ItemStack itemstack, EntityPlayer player, int inventoryIndex){
		if(!(itemstack.getItem() instanceof IBoundItem)){
			return;
		}
		itemstack = InventoryUtilities.replaceBoundItem(itemstack);
		player.inventory.setInventorySlotContents(inventoryIndex, itemstack);
	}

	public static ItemStack getApplicationStack(ItemStack boundStack){
		//handle spell for spellbook
			ItemStack castStack = SpellUtils.instance.constructSpellStack(getSpellStack(boundStack));
			castStack = SpellUtils.instance.popStackStage(castStack);
			castStack = InventoryUtilities.replaceItem(castStack, ItemsCommonProxy.spell);
			return castStack;
	}
	// return an itemstack usable by spellutils from a stored_spell tag.
	public static ItemStack getSpellStack(ItemStack stack){
		if(stack != null && stack.hasTagCompound() && stack.stackTagCompound.hasKey("stored_spell")){
			ItemStack spellstack = new ItemStack(stack.getItem(), stack.stackSize, stack.getItemDamage());
			spellstack.setTagCompound(stack.stackTagCompound.getCompoundTag("stored_spell"));
			return spellstack;
		}
		return stack;
	}
}
