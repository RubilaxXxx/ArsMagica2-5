package am2.spell.shapes;

import am2.AMCore;
import am2.api.spell.ItemSpellBase;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellCastResult;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemsCommonProxy;
import am2.network.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleHoldPosition;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.SkillData;

import am2.spell.SkillManager;
import am2.spell.SkillTreeManager;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import am2.spell.modifiers.Colour;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Glyph implements ISpellShape {

	@Override
	public int getID(){
		return 8;
	}

	@Override
	public SpellCastResult beginStackStage(ItemSpellBase item, ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, int side, boolean giveXP, int useCount){
		if (!(caster instanceof EntityPlayer)) return SpellCastResult.EFFECT_FAILED;
		ExtendedProperties ep = ExtendedProperties.For(caster);
		if (caster.isSneaking()) { // set glyphs
			if (!world.isRemote){
				double x1 = x + 0.5F;
				double y1 = y + 1.5F;
				double z1 = z + 0.5F;
				String variables = x1 + "," + y1 + "," + z1;
				if (ep.hasExtraVariable("SPELLPOS")){
					if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer)caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("ExtraGlyphs")))){
						if (!ep.hasExtraVariable("SPELLPOS2")){
							ep.addToExtraVariables("SPELLPOS2", variables);
						}else if (!ep.hasExtraVariable("SPELLPOS3")){
							ep.addToExtraVariables("SPELLPOS3", variables);
						}else{
							return SpellCastResult.EFFECT_FAILED;
						}
					}else{
						return SpellCastResult.EFFECT_FAILED;
					}
				}else{
					ep.addToExtraVariables("SPELLPOS", variables);
				}
			}
			for (int i = 0; i < AMCore.config.getGFXLevel() * 5; ++i){
				AMParticle symbols = (AMParticle)AMCore.proxy.particleManager.spawn(world, "symbols2", x + 0.5F, y + 1.5f, z + 0.5f);
				if (symbols != null){
					symbols.setMaxAge(25);
					symbols.setRGBColorF(0.85f, 0.85f, 0.85f);
					if (SpellUtils.instance.modifierIsPresent(SpellModifiers.COLOR, stack, 0)){
						ISpellModifier[] mods = SpellUtils.instance.getModifiersForStage(stack, 0);
						int ordinalCount = 0;
						for (ISpellModifier mod : mods){
							if (mod instanceof Colour){
								byte[] meta = SpellUtils.instance.getModifierMetadataFromStack(stack, mod, 0, ordinalCount++);
								symbols.setRGBColorI((int)mod.getModifier(SpellModifiers.COLOR, null, null, null, meta));
							}
						}
					}
					symbols.setParticleScale(0.5f);
					symbols.AddParticleController(new ParticleHoldPosition(symbols, 1000, 1, false));
				}
			}
		} else{ // activate glyphs
//			if (!world.isRemote) {
				if (ep.hasExtraVariable("SPELLPOS")){
					if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer)caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("ExtraGlyphs")))){
						if (ep.hasExtraVariable("SPELLPOS3")){
							String pos = ep.getExtraVariable("SPELLPOS3");
							double actualX = Double.valueOf(pos.split(",")[0]);
							double actualY = Double.valueOf(pos.split(",")[1]);
							double actualZ = Double.valueOf(pos.split(",")[2]);
							// activate glyph 3
							ItemStack newItemStack = getNewStack(stack,0);
							SpellHelper.instance.applyStackStage(newItemStack, caster, null, actualX, actualY, actualZ, 0, world, true, giveXP, 0);
							if (!world.isRemote) {
								ep.removeFromExtraVariables("SPELLPOS3");
							}
							return SpellCastResult.SUCCESS;
						}else if (ep.hasExtraVariable("SPELLPOS2")){
							String pos = ep.getExtraVariable("SPELLPOS2");
							double actualX = Double.valueOf(pos.split(",")[0]);
							double actualY = Double.valueOf(pos.split(",")[1]);
							double actualZ = Double.valueOf(pos.split(",")[2]);
							// activate glyph 2
							ItemStack newItemStack = getNewStack(stack,0);
							SpellHelper.instance.applyStackStage(newItemStack, caster, null, actualX, actualY, actualZ, 0, world, true, giveXP, 0);
							if (!world.isRemote) {
								ep.removeFromExtraVariables("SPELLPOS2");
							}
							return SpellCastResult.SUCCESS;
						}
					}
					String pos = ep.getExtraVariable("SPELLPOS");
					double actualX = Double.valueOf(pos.split(",")[0]);
					double actualY = Double.valueOf(pos.split(",")[1]);
					double actualZ = Double.valueOf(pos.split(",")[2]);
					// activate glyph 1
					ItemStack newItemStack = getNewStack(stack,0);
					SpellHelper.instance.applyStackStage(newItemStack, caster, null, actualX, actualY, actualZ, 0, world, true, giveXP, 0);
					AMNetHandler.INSTANCE.sendSpellApplyEffectToAllAround(caster, caster, actualX, actualY, actualZ, world, stack);
					if (!world.isRemote) {
						ep.removeFromExtraVariables("SPELLPOS");
					}
				}else{
					return SpellCastResult.EFFECT_FAILED;
				}
//			}
		}
		return SpellCastResult.SUCCESS;
	}

	public static ItemStack getNewStack(ItemStack oldSpell, int offset) {
		int stages = SpellUtils.instance.numStages(oldSpell);
		int stagesToTakeAway = 0;
		for (int i = 0; i < stages; i++){
			ISpellShape shape = SpellUtils.instance.getShapeForStage(oldSpell, i);
			if (shape.getClass() == Glyph.class) {
				stagesToTakeAway = i+1;
			}
		}
		stagesToTakeAway += offset;
		ItemStack newItemStack = oldSpell;
		for (int j = 0; j < stagesToTakeAway; j++) {
			newItemStack = SpellUtils.instance.popStackStage(newItemStack);
		}
		return newItemStack;
	}

	@Override
	public boolean isChanneled(){
		return false;
	}

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				new ItemStack(ItemsCommonProxy.rune, 1, ItemsCommonProxy.rune.META_WHITE),
				new ItemStack(ItemsCommonProxy.rune, 1, ItemsCommonProxy.rune.META_BLACK),
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_CHIMERITE),
				new ItemStack(ItemsCommonProxy.essence, 1, ItemsCommonProxy.essence.META_AIR),
				Items.feather,
				Items.arrow,
				Items.golden_axe,
				Items.ender_eye,
				new ItemStack(Blocks.trapped_chest)
		};
	}

	@Override
	public float manaCostMultiplier(ItemStack spellStack){
		return 1.25f;
	}

	@Override
	public boolean isTerminusShape(){
		return false;
	}

	@Override
	public boolean isPrincipumShape(){
		return true;
	}

	@Override
	public String getSoundForAffinity(Affinity affinity, ItemStack stack, World world){
		switch (affinity){
		case AIR:
			return "arsmagica2:spell.cast.air";
		case ARCANE:
			return "arsmagica2:spell.cast.arcane";
		case EARTH:
			return "arsmagica2:spell.cast.earth";
		case ENDER:
			return "arsmagica2:spell.cast.ender";
		case FIRE:
			return "arsmagica2:spell.cast.fire";
		case ICE:
			return "arsmagica2:spell.cast.ice";
		case LIFE:
			return "arsmagica2:spell.cast.life";
		case LIGHTNING:
			return "arsmagica2:spell.cast.lightning";
		case NATURE:
			return "arsmagica2:spell.cast.nature";
		case WATER:
			return "arsmagica2:spell.cast.water";
		case NONE:
		default:
			return "arsmagica2:spell.cast.none";
		}
	}
}
