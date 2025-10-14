package am2.items;

import am2.AMCore;
import am2.entities.EntityFishHookArcane;
import am2.particles.AMParticleIcons;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemArcaneFishingRod extends ArsMagicaRotatedItem {

    @SideOnly(Side.CLIENT)
    private IIcon theIcon;

    public ItemArcaneFishingRod()
    {
        super();
        this.setMaxDamage(2);
        this.setMaxStackSize(1);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
    {
        if (p_77659_3_.fishEntity != null)
        {
            int i = p_77659_3_.fishEntity.func_146034_e();
            p_77659_3_.swingItem();
        }
        else
        {
            p_77659_2_.playSoundAtEntity(p_77659_3_, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!p_77659_2_.isRemote)
            {
                p_77659_2_.spawnEntityInWorld(new EntityFishHookArcane(p_77659_2_, p_77659_3_));
            }
            p_77659_3_.swingItem();
        }

        return p_77659_1_;
    }

    @Override
    public boolean showDurabilityBar(ItemStack itemStack)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister p_94581_1_)
    {
        this.itemIcon = p_94581_1_.registerIcon(this.getIconString() + "_uncast");
        this.theIcon = p_94581_1_.registerIcon(this.getIconString() + "_cast");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage){
        if (Minecraft.getMinecraft().thePlayer.fishEntity != null && Minecraft.getMinecraft().thePlayer.getHeldItem() != null && Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemArcaneFishingRod){
            return this.theIcon;
        }else{
            return this.itemIcon;
        }
    }

    /**
     * Checks isDamagable and if it cannot be stacked
     */
    public boolean isItemTool(ItemStack p_77616_1_)
    {
        return super.isItemTool(p_77616_1_);
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return 1;
    }
}
