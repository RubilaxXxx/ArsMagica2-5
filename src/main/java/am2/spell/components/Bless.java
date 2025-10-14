package am2.spell.components;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffEffect;
import am2.buffs.BuffList;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitEntity;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellUtils;
import am2.utility.EntityUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.*;

import static am2.buffs.BuffList.buffEffectFromPotionID;

public class Bless implements ISpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){

		if (!(target instanceof EntityLivingBase) || target instanceof IBossDisplayData) return false;

		List<Integer> effectsToRemove = new ArrayList<Integer>();
		HashMap<Integer, String> effectsToMagnify = new HashMap<Integer, String>();

		Iterator iter = ((EntityLivingBase)target).getActivePotionEffects().iterator();

		int magnitudeLeft = 6 + (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack) * 2);
		int targetAmplifier = SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack);
		int targetDuration = SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration / 2, stack, caster, target, world, 0, SpellModifiers.DURATION);

		while (iter.hasNext()){
			PotionEffect potion = ((PotionEffect)iter.next());
			int potionID = potion.getPotionID();
			PotionEffect pe = ((EntityLivingBase)target).getActivePotionEffect(Potion.potionTypes[potionID]);
			if (Potion.potionTypes[potionID].isBadEffect) { // method is clientside only; we need the field
				int magnitudeCost = pe.getAmplifier();
				if (magnitudeLeft >= magnitudeCost) {
					magnitudeLeft -= magnitudeCost;
					effectsToRemove.add(potionID);
					if (pe instanceof BuffEffect && !world.isRemote) {
						((BuffEffect) pe).stopEffect((EntityLivingBase) target);
					}
				}
			} else { // good effect
				targetDuration = Math.max(targetDuration,potion.getDuration());
				targetAmplifier = Math.max(targetAmplifier,potion.getAmplifier());
				PotionEffect targetpotion = new PotionEffect(potionID,targetDuration,targetAmplifier);
				((EntityLivingBase)target).addPotionEffect(targetpotion);

				//effectsToRemove.add(potionID);
//				if (pe instanceof BuffEffect && !world.isRemote) {
//					((BuffEffect) pe).stopEffect((EntityLivingBase) target);
//				}
//				effectsToMagnify.put(potionID, pe.getDuration() + ":" + pe.getAmplifier() + ":" + (pe instanceof BuffEffect));
			}
		}

		if (!world.isRemote){
			removePotionEffects((EntityLivingBase)target, effectsToRemove);
//			for (Integer potionID : effectsToMagnify.keySet()) {
//				magnifyPotions(world, (EntityLivingBase) target, magnitudeLeft, targetAmplifier, targetDuration, potionID, Integer.parseInt(effectsToMagnify.get(potionID).split(":")[0]), Integer.parseInt(effectsToMagnify.get(potionID).split(":")[1]), Boolean.parseBoolean(effectsToMagnify.get(potionID).split(":")[2]));
//			}
		}
		return true;
	}

	public static int magnifyPotions(World world, EntityLivingBase target, int magnitudeLeft, int targetAmplifier, int targetDuration, Integer potionID, int lastDuration, int lastAmplifier, boolean lastBuffEffect) {
		if (targetAmplifier > lastAmplifier || targetDuration > lastDuration) {
			if (magnitudeLeft >= lastAmplifier) {
				magnitudeLeft -= lastAmplifier;
				if (!world.isRemote) {
					// re-add
					if (lastBuffEffect) {
						target.addPotionEffect(buffEffectFromPotionID(potionID, Math.max(targetDuration, lastDuration), Math.max(targetAmplifier, lastAmplifier)));
					} else {
						target.addPotionEffect(new PotionEffect(potionID, Math.max(targetDuration, lastDuration), Math.max(targetAmplifier, lastAmplifier)));
					}
				}
			}
		}
		return magnitudeLeft;
	}

	private void removePotionEffects(EntityLivingBase target, List<Integer> effectsToRemove){
		for (Integer i : effectsToRemove){
			target.removePotionEffect(i);
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 450;
	}

	@Override
	public float burnout(EntityLivingBase caster){
		return ArsMagicaApi.getBurnoutFromMana(manaCost(caster));
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f + rand.nextFloat() * 0.1f, 1, false));
				if (rand.nextBoolean())
					particle.setRGBColorF(0.1f, 0.9f, 0.1f);
				particle.setMaxAge(20);
				particle.setParticleScale(0.1f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public EnumSet<Affinity> getAffinity(){
		return EnumSet.of(Affinity.LIFE);
	}

	@Override
	public int getID(){
		return 99;
	}

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				new ItemStack(ItemsCommonProxy.rune, 1, ItemRune.META_PINK),
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_ARCANEASH),
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_BLUETOPAZ),
				Items.golden_apple
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0;
	}
}
