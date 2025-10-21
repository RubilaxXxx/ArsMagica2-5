package am2.armor;

import am2.armor.infusions.GenericImbuement;
import cpw.mods.fml.common.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import thaumcraft.api.IGoggles;
import thaumcraft.api.nodes.IRevealer;

@Optional.InterfaceList(
		value = { @Optional.Interface(iface = "thaumcraft.api.nodes.IRevealer", modid = "Thaumcraft",striprefs = true),
				@Optional.Interface(iface = "thaumcraft.api.IGoggles", modid = "Thaumcraft", striprefs = true)})
public class ItemMageHood extends AMArmor implements IGoggles, IRevealer{

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
