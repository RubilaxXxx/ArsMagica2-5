package am2.spell.components;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffEffectTemporalAnchor;
import am2.buffs.BuffList;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleOrbitEntity;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;

public class ChronoAnchor implements ISpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			int duration = SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration, stack, caster, target, world, 0, SpellModifiers.DURATION);
			duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
			if (!world.isRemote)
				if(!((EntityLivingBase)target).isPotionActive(BuffList.temporalAnchorBurnout.id))
					((EntityLivingBase)target).addPotionEffect(new BuffEffectTemporalAnchor(duration, SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0)));
			return true;
		}
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 80;
	}

	@Override
	public float burnout(EntityLivingBase caster){
		return ArsMagicaApi.getBurnoutFromMana(manaCost(caster));
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "clock", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.setParticleScale(0.1f);
				particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f + rand.nextFloat() * 0.1f, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
				particle.setMaxAge(40);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public EnumSet<Affinity> getAffinity(){
		return EnumSet.of(Affinity.ARCANE);
	}

	@Override
	public int getID(){
		return 6;
	}

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				new ItemStack(ItemsCommonProxy.rune, 1, ItemRune.META_WHITE),
				Items.clock,
				Items.nether_star
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.15f;
	}
}
