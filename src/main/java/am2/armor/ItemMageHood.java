package am2.armor;

import am2.armor.infusions.GenericImbuement;
import cpw.mods.fml.common.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;


@Optional.Interface(modid = "thaumcraft/api/IGoggles", iface = "Thaumcraft", striprefs = false)
public class ItemMageHood extends AMArmor{

	public ItemMageHood(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, int par4){
		super(inheritFrom, enumarmormaterial, par3, par4);
	}

	public boolean showNodes(ItemStack stack, EntityLivingBase player){
		return ArmorHelper.isInfusionPreset(stack, GenericImbuement.thaumcraftNodeReveal);
	}

	public boolean showIngamePopups(ItemStack stack, EntityLivingBase player){
		return ArmorHelper.isInfusionPreset(stack, GenericImbuement.thaumcraftNodeReveal);
	}
}
