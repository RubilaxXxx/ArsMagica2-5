package am2.blocks.tileentities;

import am2.api.power.PowerTypes;
import am2.api.spell.enums.SpellCastResult;
import am2.entities.EntityDummyCaster;
import am2.navigation.Point3D;
import am2.playerextensions.ExtendedProperties;
import am2.power.PowerNodeRegistry;
import am2.spell.SpellHelper;
import am2.utility.DummyEntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TileEntityCasterRune extends TileEntityBlockCaster{
   private int castCooldown;
   private static final int maxCastCooldown = 20;

   protected void setDummyData() {
      ExtendedProperties.For(this.dummyCaster).setMaxMana((float)this.capacity);
      this.dummyCaster.rotationPitch = 0.0F;
      this.dummyCaster.yOffset = 1.6F;
      this.dummyCaster.rotationPitch = -90.0F;
      this.dummyCaster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 1.01F), (double)((float)this.zCoord + 0.5F));
      this.pointInFront = new Point3D(this.xCoord, this.yCoord + 1, this.zCoord);
      PowerTypes highestValid = PowerTypes.NONE;
      float amt = 0;
      for (PowerTypes type1 : PowerTypes.all()){
         float tmpAmt = PowerNodeRegistry.instance.getPower(this, type1);
         if (tmpAmt > amt)
            highestValid = type1;
      }
      ExtendedProperties.For(this.dummyCaster).setCurrentMana(PowerNodeRegistry.instance.getPower(this, highestValid));
   }

   protected void tryCastSpell(ItemStack castingStack, int focusLevel) {
   }

   private void prepForActivate(){
      caster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(worldObj));
      ExtendedProperties.For(caster).setMaxMana((float)this.capacity);
      caster.rotationPitch = 0.0F;
      caster.yOffset = 1.6F;
      caster.rotationPitch = -90.0F;
      caster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 1.01F), (double)((float)this.zCoord + 0.5F));
   }

   private EntityPlayer caster;

   public void updateEntity() {
      super.updateEntity();
      if (this.castCooldown > 0) {
         --this.castCooldown;
      }

   }

   public void castSpell(Entity target) {
      if (this.castCooldown == 0) {
         int focusLevel = this.getHighestFocus();
         ItemStack castingStack = this.getCastingScrollStack();
         if (castingStack != null && target instanceof EntityLivingBase) {
            prepForActivate();
            SpellCastResult scr = SpellHelper.instance.applyStackStage(castingStack, caster, (EntityLivingBase)target, target.posX, target.posY, target.posZ, 0, this.worldObj, false, false, 0);
            if (scr == SpellCastResult.SUCCESS) {
               this.castCooldown = 20;
            }
         }
      }

   }
}
