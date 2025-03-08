package am2.blocks;

import am2.AMCore;
import am2.blocks.tileentities.TileEntityBlockCaster;
import am2.guis.ArsMagicaGuiIdList;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import am2.texture.ResourceManager;
import java.util.Random;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockCaster extends AMSpecialRenderPoweredBlock {
   public BlockCaster() {
      super(Material.rock);
      this.setHardness(3.0F);
      this.setResistance(3.0F);
   }

   public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
      super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
      if (this.HandleSpecialItems(par1World, par5EntityPlayer, par2, par3, par4)) {
         return true;
      } else {
         FMLNetworkHandler.openGui(par5EntityPlayer, AMCore.instance, ArsMagicaGuiIdList.GUI_CASTER, par1World, par2, par3, par4);
         if (par1World.isRemote){
            AMDataWriter writer = new AMDataWriter();
            writer.add(par2);
            writer.add(par3);
            writer.add(par4);
            writer.add(par5EntityPlayer.getEntityId());
            AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.CASTER_BLOCK_UPDATE, writer.generate());
         }
         return true;
      }
   }

   public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {
      int y = MathHelper.floor_double((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
      Vec3 look = par5EntityLiving.getLook(1.0F);
      int p = (int)(Math.round(look.yCoord * 0.6D) + 1L) & 3;
      byte byte0 = 3;
      if (y == 0) {
         byte0 = 0;
      } else if (y == 1) {
         byte0 = 3;
      } else if (y == 2) {
         byte0 = 2;
      } else if (y == 3) {
         byte0 = 1;
      }

      if (p == 0) {
         byte0 = (byte)(byte0 | 4);
      } else if (p == 2) {
         byte0 = (byte)(byte0 | 8);
      }

      par1World.setBlockMetadataWithNotify(par2, par3, par4, byte0, 2);
      super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLiving, par6ItemStack);
   }

   public void breakBlock(World world, int i, int j, int k, Block par5, int metadata) {
      if (world.isRemote) {
         super.breakBlock(world, i, j, k, par5, metadata);
      } else {
         Random rand = new Random();
         TileEntityBlockCaster caster = (TileEntityBlockCaster)world.getTileEntity(i, j, k);
         if (caster != null) {
            for(int l = 0; l < caster.getSizeInventory(); ++l) {
               ItemStack itemstack = caster.getStackInSlot(l);
               if (itemstack != null) {
                  float f = rand.nextFloat() * 0.8F + 0.1F;
                  float f1 = rand.nextFloat() * 0.8F + 0.1F;
                  float f2 = rand.nextFloat() * 0.8F + 0.1F;

                  while(itemstack.stackSize > 0) {
                     int i1 = rand.nextInt(21) + 10;
                     if (i1 > itemstack.stackSize) {
                        i1 = itemstack.stackSize;
                     }

                     itemstack.stackSize -= i1;
                     ItemStack newStack = new ItemStack(itemstack.getItem(), i1, itemstack.getItemDamage());
                     if (itemstack.hasTagCompound()) {
                        newStack.setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                     }

                     EntityItem entityitem = new EntityItem(world, (double)((float)i + f), (double)((float)j + f1), (double)((float)k + f2), newStack);
                     float f3 = 0.05F;
                     entityitem.motionX = (double)((float)rand.nextGaussian() * f3);
                     entityitem.motionY = (double)((float)rand.nextGaussian() * f3 + 0.2F);
                     entityitem.motionZ = (double)((float)rand.nextGaussian() * f3);
                     world.spawnEntityInWorld(entityitem);
                  }
               }
            }

            super.breakBlock(world, i, j, k, par5, metadata);
         }
      }
   }

   public TileEntity createNewTileEntity(World par1World) {
      return new TileEntityBlockCaster();
   }

   @SideOnly(Side.CLIENT)
   public void registerBlockIcons(IIconRegister par1IconRegister) {
      this.blockIcon = ResourceManager.RegisterTexture("CasterRuneSide", par1IconRegister);
   }

   @Override
   public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_){
      return new TileEntityBlockCaster();
   }
}
