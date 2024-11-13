package am2.blocks;

import am2.AMCore;
import am2.blocks.tileentities.TileEntityCandle;
import am2.blocks.tileentities.TileEntityInfusedStem;
import am2.lore.CompendiumUnlockHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleExpandingCollapsingRingAtPoint;
import am2.particles.ParticleFadeOut;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockInfusedStem extends AMFlower implements ITileEntityProvider {

	protected BlockInfusedStem(){
		super();
		this.isBlockContainer = true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int face, float interactX, float interactY, float interactZ){
		if (world.isRemote)
			CompendiumUnlockHandler.unlockEntry(this.getUnlocalizedName().replace("tile.", "").replace("arsmagica2:", ""));
		return super.onBlockActivated(world, x, y, z, player, face, interactX, interactY, interactZ);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntityInfusedStem();
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	public void onBlockAdded(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_)
	{
		super.onBlockAdded(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);
	}

	public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_)
	{
		super.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
		p_149749_1_.removeTileEntity(p_149749_2_, p_149749_3_, p_149749_4_);
	}

	public boolean onBlockEventReceived(World p_149696_1_, int p_149696_2_, int p_149696_3_, int p_149696_4_, int p_149696_5_, int p_149696_6_)
	{
		super.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_, p_149696_5_, p_149696_6_);
		TileEntity tileentity = p_149696_1_.getTileEntity(p_149696_2_, p_149696_3_, p_149696_4_);
		return tileentity != null && tileentity.receiveClientEvent(p_149696_5_, p_149696_6_);
	}
}
