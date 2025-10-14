package am2.entities;

import am2.items.ItemEssence;
import am2.items.ItemsCommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntitySpecificHallucinations {

    public static class EntityHallucinationCreeper extends EntityHallucination {

        public EntityHallucinationCreeper(World world) {
            super(world);
        }
    }

    public static class EntityHallucinationSpider extends EntityHallucination {

        public EntityHallucinationSpider(World world) {
            super(world);
        }
    }

    public static class EntityHallucinationZombie extends EntityHallucination {

        public EntityHallucinationZombie(World world) {
            super(world);
        }
    }

    public static class EntityHallucinationWitherSkeleton extends EntityHallucination {

        public EntityHallucinationWitherSkeleton(World world) {
            super(world);
            this.yOffset *= 2.0F;
            this.setSize(this.width * 2.0F, this.height * 2.0F); // lawge and scawy O.o
        }
    }

    public static class EntityHallucinationMagmacube extends EntityHallucination {

        public EntityHallucinationMagmacube(World world) {
            super(world);
        }

        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(0D); // trader entities are peaceful
        }

        public boolean attackEntityAsMob(Entity entity) {
            return true;
        }

        public boolean interact(EntityPlayer p_70085_1_)
        {
            ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();

            if (itemstack != null && itemstack.getItem() == ItemsCommonProxy.essence && !p_70085_1_.capabilities.isCreativeMode)
            {
                if (itemstack.stackSize-- == 1)
                {
                    p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemEssence.META_NIGHTMARE));
                }
                else if (!p_70085_1_.inventory.addItemStackToInventory(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemEssence.META_NIGHTMARE)))
                {
                    p_70085_1_.dropPlayerItemWithRandomChoice(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemEssence.META_NIGHTMARE), false);
                }

                return true;
            }
            else
            {
                return super.interact(p_70085_1_);
            }
        }
    }

    public static class EntityHallucinationEnderman extends EntityHallucination {

        public EntityHallucinationEnderman(World world) {
            super(world);
        }

        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(0D); // trader entities are peaceful
        }

        public boolean attackEntityAsMob(Entity entity) {
            return true;
        }

        public boolean interact(EntityPlayer p_70085_1_)
        {
            ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();

            if (itemstack != null && itemstack.getItem() == ItemsCommonProxy.itemOre && itemstack.getItemDamage() == ItemsCommonProxy.itemOre.META_SPATIALSTAR && !p_70085_1_.capabilities.isCreativeMode)
            {
                if (itemstack.stackSize-- == 1)
                {
                    p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_FRACTALFRAGMENT));
                }
                else if (!p_70085_1_.inventory.addItemStackToInventory(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_FRACTALFRAGMENT)))
                {
                    p_70085_1_.dropPlayerItemWithRandomChoice(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_FRACTALFRAGMENT), false);
                }

                return true;
            }
            else
            {
                return super.interact(p_70085_1_);
            }
        }
    }

    public static class EntityHallucinationEndermite extends EntityHallucination {

        public EntityHallucinationEndermite(World world) {
            super(world);
        }

        protected void dropFewItems(boolean recentlyHit, int looting) { // the 'evil' way of obtaining fractal fragments
            if (rand.nextInt(3) == 0) this.entityDropItem(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_FRACTALFRAGMENT), 0);
        }

        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6D);
        }
    }
}
