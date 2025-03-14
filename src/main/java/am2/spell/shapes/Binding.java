package am2.spell.shapes;

import am2.api.spell.ItemSpellBase;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellCastResult;
import am2.items.*;
import am2.spell.SpellUtils;
import am2.utility.InventoryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;


public class Binding implements ISpellShape{

	@Override
	public int getID(){
		return 2;
	}
	private final String STORED_SPELL = "stored_spell";

	@Override
	public SpellCastResult beginStackStage(ItemSpellBase item, ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, int side, boolean giveXP, int useCount){
		if (!(caster instanceof EntityPlayer)){
			return SpellCastResult.EFFECT_FAILED;
		}
		EntityPlayer player = (EntityPlayer)caster;
		ItemStack heldStack = player.getCurrentEquippedItem();
		if (heldStack == null || !(SpellUtils.instance.getShapeForStage(stack, 0) instanceof Binding)){
			return SpellCastResult.EFFECT_FAILED;
		}

		if(heldStack.getItem() != ItemsCommonProxy.spell){
			if(heldStack.getItem() instanceof ItemSpellBook){
				ItemStack storedspell = ((ItemSpellBook)heldStack.getItem()).GetActiveItemStack(heldStack);
				if (storedspell != null){
					NBTTagCompound data = (NBTTagCompound)storedspell.getTagCompound().copy();
					heldStack.stackTagCompound.setTag(STORED_SPELL, data);
				}
			}else if(heldStack.getItem() instanceof ItemSpellStaff){
				ItemStack storedspell = ((ItemSpellStaff)heldStack.getItem()).getSpellStack(heldStack);
				if(storedspell != null){
					NBTTagCompound data = (NBTTagCompound)storedspell.getTagCompound().copy();
					heldStack.stackTagCompound.setTag(STORED_SPELL, data);
				}
			}
			int itemid = Item.getIdFromItem(heldStack.getItem());
			int meta = heldStack.getItemDamage();
			heldStack.stackTagCompound.setInteger("ItemID", itemid);
			heldStack.stackTagCompound.setInteger("meta", meta);
		}
		int bindingType = getBindingType(heldStack);
			switch (bindingType){
			case ItemBindingCatalyst.META_AXE:
				heldStack = InventoryUtilities.replaceItem(heldStack, ItemsCommonProxy.BoundAxe);
				break;
			case ItemBindingCatalyst.META_PICK:
				heldStack = InventoryUtilities.replaceItem(heldStack, ItemsCommonProxy.BoundPickaxe);
				break;
			case ItemBindingCatalyst.META_SWORD:
				heldStack = InventoryUtilities.replaceItem(heldStack, ItemsCommonProxy.BoundSword);
				break;
			case ItemBindingCatalyst.META_SHOVEL:
				heldStack = InventoryUtilities.replaceItem(heldStack, ItemsCommonProxy.BoundShovel);
				break;
			case ItemBindingCatalyst.META_HOE:
				heldStack = InventoryUtilities.replaceItem(heldStack, ItemsCommonProxy.BoundHoe);
				break;
			case ItemBindingCatalyst.META_BOW:
				heldStack = InventoryUtilities.replaceItem(heldStack, ItemsCommonProxy.BoundBow);
				break;
			}
		player.inventory.setInventorySlotContents(player.inventory.currentItem, heldStack);
		return SpellCastResult.SUCCESS;
	}
	@Override
	public boolean isChanneled(){
		return false;
	}

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_CHIMERITE),
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_COGNITIVEDUST),
				Items.wooden_sword,
				Items.stone_shovel,
				Items.iron_hoe,
				Items.golden_axe,
				Items.diamond_pickaxe,
				new ItemStack(ItemsCommonProxy.bindingCatalyst, 1, Short.MAX_VALUE)
		};
	}

	@Override
	public float manaCostMultiplier(ItemStack spellStack){
		return 1;
	}

	@Override
	public boolean isTerminusShape(){
		return false;
	}

	@Override
	public boolean isPrincipumShape(){
		return true;
	}

	@Override
	public String getSoundForAffinity(Affinity affinity, ItemStack stack, World world){
		return "arsmagica2:spell.binding.cast";
	}

	public void setBindingType(ItemStack craftStack, ItemStack addedBindingCatalyst){
		SpellUtils.instance.setSpellMetadata(craftStack, "binding_type", "" + addedBindingCatalyst.getItemDamage());
	}

	public int getBindingType(ItemStack spellStack){
		int type = 0;
		try{
			type = Integer.parseInt(SpellUtils.instance.getSpellMetadata(spellStack, "binding_type"));
		}catch (Throwable t){
			t.printStackTrace();
		}
		return type;
	}
}
