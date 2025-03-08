package am2.blocks;

import am2.api.power.PowerTypes;
import am2.blocks.tileentities.TileEntityManaConsumer;
import am2.blocks.tileentities.TileEntityManaSources;
import am2.blocks.tileentities.TileEntityManaStorage;
import am2.items.ItemsCommonProxy;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public abstract class PoweredBlock extends AMBlockContainer{
	public PoweredBlock(Material material){
		super(material);
	}

	protected boolean HandleSpecialItems(World world, EntityPlayer player, int x, int y, int z){
		TileEntity te = world.getTileEntity(x, y, z);
		if (!((te instanceof TileEntityManaSources) || te instanceof TileEntityManaConsumer || te instanceof TileEntityManaStorage)){
			return false;
		}

		return player.getCurrentEquippedItem() != null && (player.getCurrentEquippedItem().getItem() == ItemsCommonProxy.spellStaffMagitech || player.getCurrentEquippedItem().getItem() == ItemsCommonProxy.crystalWrench);
	}

	protected String getColorNameFromPowerType(PowerTypes type){
		return StatCollector.translateToLocal("am2.gui.powerType" + type.name());
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9){
		super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);

		return !HandleSpecialItems(par1World, par5EntityPlayer, par2, par3, par4);
	}

}
