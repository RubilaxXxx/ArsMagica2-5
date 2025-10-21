package am2.armor;

import am2.api.items.armor.IManaGoggle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemMagitechGoggles extends AMArmor implements IManaGoggle{

	public ItemMagitechGoggles(int renderIndex){
		super(ArmorMaterial.CLOTH, ArsMagicaArmorMaterial.UNIQUE, renderIndex, 0);
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
		return 0xFFFFFF;
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot){
		return 2;
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass){
		return this.itemIcon;
	}

	@Override
	public int GetDamageReduction(){
		return 2;
	}

	@Override
	public boolean showingameHUD(World world, ItemStack stack, EntityPlayer player){
		return true;
	}
}
