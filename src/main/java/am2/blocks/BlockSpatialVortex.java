package am2.blocks;

import am2.blocks.tileentities.TileEntitySpatialVortex;
import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockSpatialVortex extends AMSpecialRenderBlockContainer{

	protected BlockSpatialVortex(){
		super(Material.cloth);
		setLightLevel(0.73f);
		setHardness(2f);
		setResistance(2f);
		setStepSound(soundTypeCloth);
		setBlockBounds(0.0f, 0.5f, 0.0f, 1.0f, 2f, 1.0f);
	}

	@Override
	public int quantityDropped(Random random){
		return 1;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z){
		return 14;
	}

	@Override
	public TileEntity createNewTileEntity(World par1World, int i){
		return new TileEntitySpatialVortex();
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister){
	}

	@Override
	public int getRenderType(){
		return BlocksCommonProxy.blockRenderID;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		return null;
	}

	@Override
	public void addCollisionBoxesToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {
		return;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World arg0, int arg1, int arg2, int arg3, int arg4, int arg5){
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(ItemsCommonProxy.itemOre, 2, ItemOre.META_PURIFIEDVINTEUM));
		return drops;
	}
}
