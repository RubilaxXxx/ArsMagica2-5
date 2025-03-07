package am2.armor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemMagitechGoggles extends AMArmor{

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

	public MovingObjectPosition getMOP(World world,EntityPlayer player){
		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, false);
		if(mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
			return mop;
		}
		return null;
	}
	@Override
	public int GetDamageReduction(){
		return 2;
	}

}
