package am2.entities;

import am2.items.ItemEssence;
import am2.items.ItemsCommonProxy;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityHallucination extends EntityMob {

    private EntityPlayer TargetPlayer = null;

    public EntityHallucination(World world) {
        super(world);
        super.isImmuneToFire = true;
        super.tasks.addTask(1, new EntityAISwimming(this));
        super.tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0D, false));
        super.tasks.addTask(3, new EntityAIWander(this, 0.8D));
        super.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        super.tasks.addTask(5, new EntityAILookIdle(this));
        super.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
    }

    public String getCommandSenderName() {
        return this.hasCustomNameTag() ? this.getCustomNameTag() : StatCollector.translateToLocal("am2.entity.hallucination");
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(750D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3D);
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1);
    }

    public boolean isAIEnabled() {
        return true;
    }

    public int getTalkInterval() {
        return super.getTalkInterval() * 2;
    }

    public EntityLivingBase getAttackTarget() {
        return super.worldObj.getPlayerEntityByName(this.getTargetName());
    }

    public int getMaxSafePointTries() {
        return this.getAttackTarget() == null ? 3 : 3 + (int)(this.getHealth() - 1.0F);
    }

    protected void entityInit() {
        super.entityInit();
        super.dataWatcher.addObject(17, "");
        super.dataWatcher.addObject(18, Byte.valueOf((byte)0));
    }

    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeEntityToNBT(par1NBTTagCompound);
        if(this.getTargetName() == null) {
            par1NBTTagCompound.setString("Target", "");
        } else {
            par1NBTTagCompound.setString("Target", this.getTargetName());
        }

        par1NBTTagCompound.setInteger("HallucinationType", this.getHallucinationType());
    }

    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        String s = par1NBTTagCompound.getString("Target");
        if(s.length() > 0) {
            this.setTarget(s);
        }

        this.setHallucinationType(par1NBTTagCompound.getInteger("HallucinationType"));
    }

    public String getTargetName() {
        return super.dataWatcher.getWatchableObjectString(17);
    }

    public void setTarget(String par1Str) {
        super.dataWatcher.updateObject(17, par1Str);
    }

    protected String getDeathSound() {
        return null;
    }

    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        if (par1DamageSource.isMagicDamage()) {
            return super.attackEntityFrom(par1DamageSource, par2);
        }
        return false;
    }

    public int getHallucinationType() {
        return super.dataWatcher.getWatchableObjectByte(18);
    }

    public void setHallucinationType(int par1) {
        super.dataWatcher.updateObject(18, Byte.valueOf((byte)par1));
    }

    public void onUpdate() {
        super.onUpdate();
    }

    protected String getLivingSound() {
        return null;
    }

    protected String getHurtSound() {
        return null;
    }

    protected void dropFewItems(boolean recentlyHit, int looting) {
        if (rand.nextInt(3) == 0) this.entityDropItem(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemEssence.META_NIGHTMARE), 0);
    }


}
