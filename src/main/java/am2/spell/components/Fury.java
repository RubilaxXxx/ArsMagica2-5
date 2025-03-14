package am2.spell.components;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffEffectFury;
import am2.buffs.BuffList;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleOrbitEntity;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;

public class Fury implements ISpellComponent, IRitualInteraction{

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				Items.fish,
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_HELLFISH),
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_SUNSTONE)
		};
	}

	@Override
	public int getID(){
		return 72;
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			int duration = SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration, stack, caster, target, world, 0, SpellModifiers.DURATION);
			duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);

			int x = (int)Math.floor(target.posX);
			int y = (int)Math.floor(target.posY);
			int z = (int)Math.floor(target.posZ);
			if (RitualShapeHelper.instance.checkForRitual(this, world, x, y, z) != null){
				duration += (3600 * (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 1));
				RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
			}

			if (!world.isRemote){
				((EntityLivingBase)target).addPotionEffect(new BuffEffectFury(duration, 0));
			}
			return true;
		}
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 261;
	}

	@Override
	public float burnout(EntityLivingBase caster){
		return 1000;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 5 * AMCore.config.getGFXLevel(); ++i){
			AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "pulse", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.setRGBColorF(1, 0, 0);
				particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.15f, 1, false).SetTargetDistance(world.rand.nextDouble() + 1f).setIgnoreYCoordinate(true));
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.1f, 1, false));
				particle.setMaxAge(10);
			}
		}
	}

	@Override
	public EnumSet<Affinity> getAffinity(){
		return EnumSet.of(Affinity.FIRE, Affinity.LIGHTNING);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape(){
		return RitualShapeHelper.instance.hourglass;
	}

	@Override
	public ItemStack[] getReagents(){
		return new ItemStack[]{
				new ItemStack(Items.potionitem, 1, 8194),
				new ItemStack(Items.potionitem, 1, 8201)
		};
	}

	@Override
	public int getReagentSearchRadius(){
		return 3;
	}

}
