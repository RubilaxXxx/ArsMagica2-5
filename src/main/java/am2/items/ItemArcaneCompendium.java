package am2.items;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.guis.AMGuiHelper;
import am2.network.AMNetHandler;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.SkillData;
import am2.spell.SkillManager;
import am2.texture.ResourceManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemArcaneCompendium extends ArsMagicaItem{

	public ItemArcaneCompendium(){
		super();
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
		if (world.isRemote){
			AMGuiHelper.OpenCompendiumGui(stack);
		}
		if (!world.isRemote && player !=null && ExtendedProperties.For(player).getMagicLevel() <= 0){
			player.addChatMessage(new ChatComponentText("You have unlocked the secrets of the arcane!"));
			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)player, "shapes", true);
			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)player, "components", true);
			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)player, "modifiers", true);
			ExtendedProperties.For(player).setMagicLevelWithMana(1);
			ExtendedProperties.For(player).forceSync();
			if(AMCore.config.isEasyStart()){
				ISkillTreeEntry touch = SkillManager.instance.getSkill("Touch");
				ISkillTreeEntry self = SkillManager.instance.getSkill("Self");
				ISkillTreeEntry proj = SkillManager.instance.getSkill("Projectile");
				SkillData.For(player).learn(touch);
				SkillData.For(player).learn(self);
				SkillData.For(player).learn(proj);
			}
		}
		return stack;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = ResourceManager.RegisterTexture("arcanecompendium", par1IconRegister);
	}
}
