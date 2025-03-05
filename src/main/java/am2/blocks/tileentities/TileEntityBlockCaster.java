package am2.blocks.tileentities;

import am2.api.power.PowerTypes;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.SpellCastResult;
import am2.entities.EntityDummyCaster;
import am2.items.ISpellFocus;
import am2.items.ItemFocusCharge;
import am2.navigation.Point3D;
import am2.network.AMDataWriter;
import am2.playerextensions.ExtendedProperties;
import am2.power.PowerNodeRegistry;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import am2.utility.DummyEntityPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TileEntityBlockCaster extends TileEntityAMManaPower implements IInventory {
   //TAGS
   public static final String TAG_POWERAMOUNT = "PowerAmount";

   //Vars
   private int poweramount;
   protected float rotation = 0.0F;
   protected ItemStack[] casterItemStacks = new ItemStack[4];
   protected boolean hasRequestedFullUpdate = false;
   protected int ticksChanneled = 0;
   protected EntityLiving dummyCaster;
   protected boolean hasCast;
   protected boolean redstonePowerLastTick;
   protected Point3D pointInFront;
   private int activeTicks = 0;
   private boolean firstTick = true;

   public TileEntityBlockCaster() {
      super(10000);
      this.dummyCaster = new EntityDummyCaster(this.worldObj);
      this.hasCast = false;
      this.redstonePowerLastTick = false;
   }

   protected ItemStack getCastingScrollStack() {
      return this.casterItemStacks[3] == null ? null : this.casterItemStacks[3];
   }

   public boolean canProvidePower() {
      return false;
   }

   public int getCharge() {
    return this.poweramount;
   }


   protected int getHighestFocus() {
      int focusLevel = -1;
      for(int i = 0; i < 3; ++i) {
         if (this.casterItemStacks[i] != null && this.casterItemStacks[i].getItem() instanceof ISpellFocus) {
            int tempFocusLevel = ((ISpellFocus)this.casterItemStacks[i].getItem()).getFocusLevel();
            if (tempFocusLevel > focusLevel) {
               focusLevel = tempFocusLevel;
            }
         }
      }

      return focusLevel;
   }

   private int numFociOfType(Class type) {
      int count = 0;

      for(int i = 0; i < 3; ++i) {
         if (this.casterItemStacks[i] != null && type.isInstance(this.casterItemStacks[i].getItem())) {
            ++count;
         }
      }

      return count;
   }

   protected void setDummyData() {
      this.dummyCaster.worldObj = this.worldObj;
      ExtendedProperties.For(this.dummyCaster).setMaxMana((float)this.capacity);
      int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
      this.dummyCaster.rotationPitch = 0.0F;
      this.dummyCaster.yOffset = 1.6F;
      switch(meta & 3) {
      case 0:
         this.dummyCaster.rotationYaw = 180.0F;
         this.dummyCaster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord - 0.01F));
         this.pointInFront = new Point3D(this.xCoord, this.yCoord, this.zCoord - 1);
         break;
      case 1:
         this.dummyCaster.rotationYaw = 90.0F;
         this.dummyCaster.setPosition((double)((float)this.xCoord - 0.01F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord + 0.5F));
         this.pointInFront = new Point3D(this.xCoord - 1, this.yCoord, this.zCoord);
         break;
      case 2:
         this.dummyCaster.rotationYaw = 0.0F;
         this.dummyCaster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord + 1.01F));
         this.pointInFront = new Point3D(this.xCoord, this.yCoord, this.zCoord + 1);
         break;
      case 3:
         this.dummyCaster.rotationYaw = 270.0F;
         this.dummyCaster.setPosition((double)((float)this.xCoord + 1.01F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord + 0.5F));
         this.pointInFront = new Point3D(this.xCoord + 1, this.yCoord, this.zCoord);
      }

      int mV = (meta & 12) >> 2;
      if (mV == 1) {
         this.dummyCaster.rotationPitch = -90.0F;
         this.dummyCaster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 1.01F), (double)((float)this.zCoord + 0.5F));
         this.pointInFront = new Point3D(this.xCoord, this.yCoord + 1, this.zCoord);
      } else if (mV == 2) {
         this.dummyCaster.rotationPitch = 90.0F;
         this.dummyCaster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord - 0.01F), (double)((float)this.zCoord + 0.5F));
         this.pointInFront = new Point3D(this.xCoord, this.yCoord - 1, this.zCoord);
      }

      ExtendedProperties.For(this.dummyCaster).setCurrentMana(this.getCharge());
   }

   public float getCastCost() {
      ItemStack castingStack = this.getCastingScrollStack();
      return castingStack != null ? SpellUtils.instance.getSpellRequirements(this.getCastingScrollStack(), this.dummyCaster).manaCost : -1.0F;
   }

   public void updateEntity() {
      if (this.worldObj != null) {

         if (this.firstTick) {
            this.firstTick = false;
            ExtendedProperties.For(this.dummyCaster).init(this.dummyCaster, this.worldObj);
         }

         this.setDummyData();
         ItemStack castingStack = this.getCastingScrollStack();
         if (!this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord)) {
            if (this.hasCast) {
               if (this.worldObj.isRemote && castingStack != null && SpellUtils.instance.spellIsChanneled(castingStack)) {
                  playSound(castingStack);
               }

               this.hasCast = false;
            }

            this.activeTicks = 0;
         } else {
            int focusLevel = this.getHighestFocus();
            if (focusLevel != -1 && castingStack != null) {
               this.tryCastSpell(castingStack, focusLevel);
            }
         }

         if (this.worldObj.isRemote && this.hasCharge()) {
            this.incrementRotation();
         }

         super.updateEntity();
      }
   }

   @SideOnly(Side.CLIENT)
   private void playSound(ItemStack castingStack) {
      ISpellShape shape = SpellUtils.instance.getShapeForStage(castingStack, 0);
      net.minecraft.client.Minecraft.getMinecraft().getSoundHandler().stopSound(net.minecraft.client.audio.PositionedSoundRecord.func_147674_a(new ResourceLocation(shape.getSoundForAffinity(SpellUtils.instance.mainAffinityFor(castingStack), castingStack, (World)null)), 1.0F));
   }

   public boolean hasCharge() {
      return this.getCharge() > this.getCastCost();
   }

   protected void tryCastSpell(ItemStack castingStack, int focusLevel) {
      float castCost = this.getCastCost();
      if (this.getCharge() > castCost) {
         prepForActivate();
         if (SpellUtils.instance.spellIsChanneled(castingStack) && SpellHelper.instance.applyStackStageOnUsing(castingStack, caster, caster, (double)this.xCoord, (double)this.yCoord, (double)this.zCoord, this.worldObj, false, false, this.activeTicks++) == SpellCastResult.SUCCESS) {
            PowerNodeRegistry.instance.consumePower(this, getBestType(), castCost);
            this.hasCast = true;
         } else if (!this.hasCast && SpellHelper.instance.applyStackStage(castingStack, caster, caster, (double)this.xCoord, (double)this.yCoord, (double)this.zCoord, 0, this.worldObj, false, false, 0) == SpellCastResult.SUCCESS) {
            PowerNodeRegistry.instance.consumePower(this, getBestType(), castCost);
            this.hasCast = true;
         }
      }

   }

   private EntityPlayer caster;

   private void prepForActivate(){
      caster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(worldObj));
      ExtendedProperties.For(caster).setMaxMana((float)this.capacity);
      ExtendedProperties.For(caster).setCurrentMana((float)this.getCharge());
      int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
      caster.rotationPitch = 0.0F;
      caster.yOffset = 1.6F;
      switch(meta & 3) {
      case 0:
         caster.rotationYaw = 180.0F;
         caster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord - 0.01F));
         break;
      case 1:
         caster.rotationYaw = 90.0F;
         caster.setPosition((double)((float)this.xCoord - 0.01F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord + 0.5F));
         break;
      case 2:
         caster.rotationYaw = 0.0F;
         caster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord + 1.01F));
         break;
      case 3:
         caster.rotationYaw = 270.0F;
         caster.setPosition((double)((float)this.xCoord + 1.01F), (double)((float)this.yCoord + 0.8F), (double)((float)this.zCoord + 0.5F));
      }

      int mV = (meta & 12) >> 2;
      if (mV == 1) {
         caster.rotationPitch = -90.0F;
         caster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord + 1.01F), (double)((float)this.zCoord + 0.5F));
      } else if (mV == 2) {
         caster.rotationPitch = 90.0F;
         caster.setPosition((double)((float)this.xCoord + 0.5F), (double)((float)this.yCoord - 0.01F), (double)((float)this.zCoord + 0.5F));
      }
   }

   private PowerTypes getBestType(){
      PowerTypes highestValid = PowerTypes.NONE;
      float amt = 0;
      for (PowerTypes type1 : PowerTypes.all()){
         float tmpAmt = PowerNodeRegistry.instance.getPower(this, type1);
         if (tmpAmt > amt)
            highestValid = type1;
      }
      return highestValid;
   }

   protected int getChargeThreshold() {
      return this.capacity;
   }

   public void incrementRotation() {
      this.rotation = (this.rotation + 0.01F) % 360.0F;
   }

   public float getRotation() {
      return this.rotation;
   }

   public boolean isUseableByPlayer(EntityPlayer entityplayer) {
      if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this) {
         return false;
      } else {
         return entityplayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
      }
   }

   @Override
   public void openInventory(){

   }

   @Override
   public void closeInventory(){

   }

   public int getSizeInventory() {
      return 4;
   }

   public ItemStack getStackInSlot(int var1) {
      return var1 >= this.casterItemStacks.length ? null : this.casterItemStacks[var1];
   }

   public ItemStack decrStackSize(int i, int j) {
      if (this.casterItemStacks[i] != null) {
         ItemStack itemstack1;
         if (this.casterItemStacks[i].stackSize <= j) {
            itemstack1 = this.casterItemStacks[i];
            this.casterItemStacks[i] = null;
            return itemstack1;
         } else {
            itemstack1 = this.casterItemStacks[i].splitStack(j);
            if (this.casterItemStacks[i].stackSize == 0) {
               this.casterItemStacks[i] = null;
            }

            return itemstack1;
         }
      } else {
         return null;
      }
   }

   public ItemStack getStackInSlotOnClosing(int i) {
      if (this.casterItemStacks[i] != null) {
         ItemStack itemstack = this.casterItemStacks[i];
         this.casterItemStacks[i] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int i, ItemStack itemstack) {
      this.casterItemStacks[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

   }

   @Override
   public String getInventoryName(){
      return null;
   }

   @Override
   public boolean hasCustomInventoryName(){
      return false;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      NBTTagList nbttaglist = (NBTTagList)nbttagcompound.getTag("CasterInventory");
      this.casterItemStacks = new ItemStack[this.getSizeInventory()];
      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         String tag = String.format("ArrayIndex", i);
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte byte0 = nbttagcompound1.getByte(tag);
         if (byte0 >= 0 && byte0 < this.casterItemStacks.length) {
            this.casterItemStacks[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
         }
      }

   }

   public void writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.casterItemStacks.length; ++i) {
         if (this.casterItemStacks[i] != null) {
            String tag = String.format("ArrayIndex", i);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte(tag, (byte)i);
            this.casterItemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("CasterInventory", nbttaglist);
   }

   public String getInvName() {
      return "Caster";
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   public void openChest() {
   }

   public void closeChest() {
   }

   public int getPowerPerChargeTick() {
      int numFoci = this.numFociOfType(ItemFocusCharge.class);
      int base = 10;
      if (numFoci > 0) {
         base *= 5 * numFoci;
      }

      return base;
   }

   public byte[] GetUpdatePacketForClient() {
      AMDataWriter writer = new AMDataWriter();
      this.writeInventory(writer);
      return writer.generate();
   }

   private void writeInventory(AMDataWriter writer) {
      ItemStack[] arr$ = this.casterItemStacks;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ItemStack stack = arr$[i$];
         if (stack == null) {
            writer.add(false);
         } else {
            writer.add(true);
            writer.add(stack);
         }
      }

   }

   public boolean isInvNameLocalized() {
      return false;
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   @Override
   public boolean canRelayPower(PowerTypes type){
      return true;
   }

   @Override
   public int getChargeRate(){
      return 250;
   }
}
