package am2.blocks;

import am2.api.power.IManaPower;
import am2.api.power.PowerTypes;
import am2.blocks.tileentities.TileEntityAMManaPower;
import am2.blocks.tileentities.TileEntityManaBattery;
import am2.blocks.tileentities.TileEntityObelisk;
import am2.blocks.tileentities.TileEntityPowerSources;
import am2.items.ItemsCommonProxy;
import am2.power.PowerNodeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.ArrayList;

public abstract class PoweredBlock extends AMBlockContainer{
	public PoweredBlock(Material material){
		super(material);
	}

	protected boolean HandleSpecialItems(World world, EntityPlayer player, int x, int y, int z){
		TileEntity te = world.getTileEntity(x, y, z);
		if (!((te instanceof TileEntityPowerSources) || te instanceof TileEntityAMManaPower)){
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
