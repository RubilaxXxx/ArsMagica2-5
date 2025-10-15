package am2.items;

import am2.particles.AMParticleIcons;
import am2.texture.ResourceManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemOre extends ArsMagicaItem{

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	@SideOnly(Side.CLIENT)
	private String[] textures;

	public static final int META_VINTEUMDUST = 0;
	public static final int META_ARCANECOMPOUND = 1;
	public static final int META_ARCANEASH = 2;
	public static final int META_PURIFIEDVINTEUM = 3;
	public static final int META_CHIMERITE = 4;
	public static final int META_BLUETOPAZ = 5;
	public static final int META_SUNSTONE = 6;
	public static final int META_MOONSTONE = 7;
	public static final int META_ANIMALFAT = 8;
	public static final int META_CELESTIALFISH = 9;
	public static final int META_COSMICDUST = 10;
	public static final int META_MOONSTONEFRAGMENT = 11;
	public static final int META_SUNSTONEFRAGMENT = 12;
	public static final int META_HELLFISH = 13;
	public static final int META_PYROGENICSEDIMENT = 14;
	public static final int META_SPATIALSTAR = 15;
	public static final int META_TEMPORALCLUSTER = 16;
	public static final int META_STORMSAWTOOTH = 17;
	public static final int META_RAINROCKROSE = 18;
	public static final int META_IMBUEDMOONFLOWER = 19;
	public static final int META_COGNITIVEDUST = 20;
	public static final int META_FRACTALFRAGMENT = 21;
	public static final int META_SOULFRAGMENT = 22;

	public ItemOre(){
		super();
		this.setHasSubtypes(true);
	}

	@Override
	public boolean isPotionIngredient(ItemStack stack){
		switch (stack.getItemDamage()){
		case META_VINTEUMDUST:
		case META_ARCANEASH:
		case META_PURIFIEDVINTEUM:
			return true;
		}
		return false;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		int meta = par1ItemStack.getItemDamage();
		switch (meta){
		case META_VINTEUMDUST:
			return StatCollector.translateToLocal("item.arsmagica2:vinteumDust.name");
		case META_ARCANECOMPOUND:
			return StatCollector.translateToLocal("item.arsmagica2:arcaneCompound.name");
		case META_ARCANEASH:
			return StatCollector.translateToLocal("item.arsmagica2:arcaneAsh.name");
		case META_PURIFIEDVINTEUM:
			return StatCollector.translateToLocal("item.arsmagica2:purifiedVinteumDust.name");
		case META_CHIMERITE:
			return StatCollector.translateToLocal("item.arsmagica2:chimerite.name");
		case META_BLUETOPAZ:
			return StatCollector.translateToLocal("item.arsmagica2:blueTopaz.name");
		case META_SUNSTONE:
			return StatCollector.translateToLocal("item.arsmagica2:sunstone.name");
		case META_MOONSTONE:
			return StatCollector.translateToLocal("item.arsmagica2:moonstone.name");
		case META_ANIMALFAT:
			return StatCollector.translateToLocal("item.arsmagica2:animalfat.name");
		case META_CELESTIALFISH:
			return StatCollector.translateToLocal("item.arsmagica2:celestialfish.name");
		case META_COSMICDUST:
			return StatCollector.translateToLocal("item.arsmagica2:cosmicdust.name");
		case META_MOONSTONEFRAGMENT:
			return StatCollector.translateToLocal("item.arsmagica2:moonstonefragment.name");
		case META_SUNSTONEFRAGMENT:
			return StatCollector.translateToLocal("item.arsmagica2:sunstonefragment.name");
		case META_HELLFISH:
			return StatCollector.translateToLocal("item.arsmagica2:hellfish.name");
		case META_PYROGENICSEDIMENT:
			return StatCollector.translateToLocal("item.arsmagica2:pyrogenicsediment.name");
		case META_SPATIALSTAR:
			return StatCollector.translateToLocal("item.arsmagica2:spatialstar.name");
		case META_TEMPORALCLUSTER:
			return StatCollector.translateToLocal("item.arsmagica2:temporalcluster.name");
		case META_STORMSAWTOOTH:
			return StatCollector.translateToLocal("item.arsmagica2:stormsawtooth.name");
		case META_RAINROCKROSE:
			return StatCollector.translateToLocal("item.arsmagica2:rainrockrose.name");
		case META_IMBUEDMOONFLOWER:
			return StatCollector.translateToLocal("item.arsmagica2:imbuedmoonflower.name");
		case META_COGNITIVEDUST:
			return StatCollector.translateToLocal("item.arsmagica2:cognitivedust.name");
		case META_FRACTALFRAGMENT:
			return StatCollector.translateToLocal("item.arsmagica2:fractalfragment.name");
		case META_SOULFRAGMENT:
			return StatCollector.translateToLocal("item.arsmagica2:soulfragment.name");
		}

		return super.getItemStackDisplayName(par1ItemStack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		textures = new String[]{"vinteum_dust", "arcane_compound", "arcane_ash", "purified_vinteum", "chimerite_gem", "blue_topaz_gem", "sunstone_gem", "moonstone_gem", "animalFat", "celestial_fish", "cosmic_dust", "moonstone_fragment", "sunstone_fragment", "hellfish", "pyrogenic_sediment", "spatial_star", "temporal_cluster", "stormSawtooth_stage_2", "rainRockrose_stage_2", "imbuedMoonflower_stage_2", "cognitive_dust", "fractal_fragment", "soul_fragment"};

		icons = new IIcon[textures.length];
		for(int i = 0; i < textures.length; i++){
			icons[i] = ResourceManager.RegisterTexture(textures[i], par1IconRegister);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta){
		if(meta < icons.length){
			return icons[meta];
		}
		return AMParticleIcons.instance.getIconByName("mystic");
	}

	@Override
	public String getPotionEffect(ItemStack stack){
		switch (stack.getItemDamage()){
		case META_VINTEUMDUST:
			return "+0+1+2-3&4-4+13";
		case META_ARCANEASH:
			return "+0+1-2+3&4-4+13";
		case META_PURIFIEDVINTEUM:
			return "+0-1+2+3&4-4+13";
		}
		return "";
	}

	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack p_77636_1_)
	{
		return p_77636_1_.getItemDamage() == META_SPATIALSTAR || p_77636_1_.getItemDamage() == META_SOULFRAGMENT || p_77636_1_.getItemDamage() == META_FRACTALFRAGMENT;
	}

	/**
	 * Return an item rarity from EnumRarity
	 */
	public EnumRarity getRarity(ItemStack p_77613_1_)
	{
		return (p_77613_1_.getItemDamage() == META_SPATIALSTAR || p_77613_1_.getItemDamage() == META_TEMPORALCLUSTER || p_77613_1_.getItemDamage() == META_SOULFRAGMENT || p_77613_1_.getItemDamage() == META_FRACTALFRAGMENT) ? EnumRarity.epic : EnumRarity.common;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List){
		for (int i = 0; i < icons.length; ++i){
			par3List.add(new ItemStack(this, 1, i));
		}
	}
}
