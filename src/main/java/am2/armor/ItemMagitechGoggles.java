package am2.armor;

import am2.api.power.IManaPower;
import am2.api.power.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import static am2.network.AMNetHandler.sendTiledatatoClient;

public class ItemMagitechGoggles extends AMArmor{
	private int ticks;

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
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack){
		if(armorType == 0 && itemStack.getItem() == this){

			if (ticks++ % 40 == 0){
			ticks = 1;
			MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, false);
		if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
				TileEntity tile = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
					if (tile instanceof IWrenchable && !world.isRemote && player instanceof EntityPlayerMP){
						sendTiledatatoClient(tile, (EntityPlayerMP) player);
					}
				}
			}
		}
	}

	@Override
	public int GetDamageReduction(){
		return 2;
	}
}
