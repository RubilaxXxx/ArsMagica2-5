package am2.items;

import am2.api.items.IBoundItem;
import am2.api.items.ManaItemHandler;
import am2.entities.EntityBoundArrow;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import am2.texture.ResourceManager;
import am2.utility.InventoryUtilities;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBoundBow extends ItemBow implements IBoundItem{

	public ItemBoundBow(){
		this.setMaxDamage(0);
	}

	public ItemBoundBow setUnlocalizedAndTextureName(String name){
		this.setUnlocalizedName(name);
		setTextureName(name);
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = ResourceManager.RegisterTexture("bound_bow", par1IconRegister);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack par1ItemStack){
		return EnumRarity.rare;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack){
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack){
		return false;
	}

	@Override
	public int getItemEnchantability(){
		return 0;
	}

	@Override
	public boolean isItemTool(ItemStack par1ItemStack){
		return true;
	}

	@Override
	public boolean isRepairable(){
		return false;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player){
		UnbindItem(item, player, player.inventory.currentItem);
		return false;
	}

	@Override
	public float maintainCost(){
		return IBoundItem.normalMaintain;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int slotIndex, boolean par5){
		if (par3Entity instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)par3Entity;
			if (player.capabilities.isCreativeMode) return;
			ExtendedProperties props = ExtendedProperties.For(player);
			if (ManaItemHandler.canExtractMana(par1ItemStack, player,maintainCost())){
				UnbindItem(par1ItemStack, (EntityPlayer)par3Entity, slotIndex);
				return;
			}else{
				props.deductMana(this.maintainCost());
			}
			if (par1ItemStack.getItemDamage() > 0)
				par1ItemStack.damageItem(-1, (EntityLivingBase)par3Entity);
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
	{
		p_77659_3_.setItemInUse(p_77659_1_, this.getMaxItemUseDuration(p_77659_1_)); // do not need a real arrow to fire it - it's a magic manifestation, after all
		return p_77659_1_;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_)
	{
		int j = this.getMaxItemUseDuration(p_77615_1_) - p_77615_4_;

		float f = (float)j / 10.0F;
		f = (f * f + f * 2.0F) / 3.0F;

		if ((double)f < 0.1D)
		{
			return;
		}

		if (f > 1.0F)
		{
			f = 1.0F;
		}

		EntityBoundArrow entityarrow = new EntityBoundArrow(p_77615_2_, p_77615_3_, f * 2.0F, getApplicationStack(p_77615_1_));

		if (f == 1.0F)
		{
			entityarrow.setIsCritical(true);
		}

		int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, p_77615_1_);

		if (k > 0)
		{
			entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5D + 0.5D);
		}

		int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, p_77615_1_);

		if (l > 0)
		{
			entityarrow.setKnockbackStrength(l);
		}

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, p_77615_1_) > 0)
		{
			entityarrow.setFire(100);
		}

		p_77615_2_.playSoundAtEntity(p_77615_3_, "arsmagica2:spell.cast.air", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
		entityarrow.canBePickedUp = 2; // no picking up manifested magical arrows!

		if (!p_77615_2_.isRemote)
		{
			p_77615_2_.spawnEntityInWorld(entityarrow);
		}
	}

	@Override
	public void UnbindItem(ItemStack itemstack, EntityPlayer player, int inventorySlot){
		itemstack = InventoryUtilities.replaceBoundItem(itemstack);
		player.inventory.setInventorySlotContents(inventorySlot, itemstack);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){ // you can apply the effect just by hitting someone with the bow (or by shooting them!)
		if (!player.isSneaking() && stack.hasTagCompound() && entity instanceof EntityLivingBase){
			ItemStack castStack = getApplicationStack(stack);
			SpellHelper.instance.applyStackStage(castStack, player, (EntityLivingBase)entity, entity.posX, entity.posY, entity.posZ, 0, player.worldObj, true, true, 0);
		}
		return super.onLeftClickEntity(stack, player, entity);
	}

	private ItemStack getApplicationStack(ItemStack boundStack){
		ItemStack castStack = SpellUtils.instance.constructSpellStack(boundStack.copy());
		castStack = SpellUtils.instance.popStackStage(castStack);
		castStack = InventoryUtilities.replaceItem(castStack, ItemsCommonProxy.spell);

		return castStack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRotateAroundWhenRendering(){
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D(){
		return true;
	}
}
