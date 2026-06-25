package am2.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

public class EnchantMagicResist extends Enchantment{

	public EnchantMagicResist(int effectID, int weight){
		super(effectID, weight, EnumEnchantmentType.armor);
		setName("magicresist");
	}

	@Override
	public int getMaxLevel(){
		return 5;
	}
	public int calcModifierDamage(int level, DamageSource source){
		if(!source.isMagicDamage()) return 0;
		else {
			float f = (float)(6 + level * level) / 3.0F;
			return MathHelper.floor_float(f * 0.75F);
		}

	}
}
