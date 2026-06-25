package am2.spell;

import am2.AMCore;
import am2.api.events.SpellCastingEvent;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.enums.SkillPointTypes;
import am2.api.spell.enums.SpellModifiers;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.SkillData;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class SpellUnlockManager{

	private ArrayList<UnlockEntry> entries;

	public SpellUnlockManager(){
		init();
	}

	@SubscribeEvent
	public void onSpellCast(SpellCastingEvent.Pre event){
		if (event.caster instanceof EntityPlayer){
			EntityPlayer caster = (EntityPlayer)event.caster;
			if (ExtendedProperties.For(caster).getCurrentMana() < event.manaCost)
				return;
			for (UnlockEntry entry : entries){
				//check unlocks
				if (!caster.worldObj.isRemote){
					if (entry.unlockIsInPrimaryTree(caster) && entry.willSpellUnlock(event.stack) && entry.isGuardianKilled(caster)){
						entry.unlockFor(caster);
					}
				}
				if (!(entry.unlock instanceof ISpellModifier)){
					//lock out casting of spells that contain "silver" skills you don't know, or pre-learned ones (somehow) that aren't in your primary tree
					if (entry.unlockIsInSpell(event.stack) && (!SkillData.For(caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(entry.unlock)) || !entry.unlockIsInPrimaryTree(caster))){
						event.setCanceled(true);
						return;
					}
				}
			}
		}
	}

	public void init(){
		entries = new ArrayList<UnlockEntry>();
		entries.add(new UnlockEntry(7, SkillManager.instance.getSkill("FallingStar"), SkillManager.instance.getSkill("MagicDamage"), SkillManager.instance.getSkill("Gravity"), SkillManager.instance.getSkill("AstralDistortion")));
		entries.add(new UnlockEntry(8, SkillManager.instance.getSkill("Blizzard"), SkillManager.instance.getSkill("Storm"), SkillManager.instance.getSkill("FrostDamage"), SkillManager.instance.getSkill("Freeze"), SkillManager.instance.getSkill("Damage")));
		entries.add(new UnlockEntry(9, SkillManager.instance.getSkill("FireRain"), SkillManager.instance.getSkill("Storm"), SkillManager.instance.getSkill("FireDamage"), SkillManager.instance.getSkill("Ignition"), SkillManager.instance.getSkill("Damage")));
		entries.add(new UnlockEntry(10, SkillManager.instance.getSkill("ConcentrateTime"), SkillManager.instance.getSkill("Accelerate"), SkillManager.instance.getSkill("Haste"), SkillManager.instance.getSkill("Lingering")));
		entries.add(new UnlockEntry(2, SkillManager.instance.getSkill("Dismembering"), SkillManager.instance.getSkill("Piercing"), SkillManager.instance.getSkill("Damage")));

		entries.add(new UnlockEntry(4, SkillManager.instance.getSkill("ManaLink"), SkillManager.instance.getSkill("ManaDrain"), SkillManager.instance.getSkill("Entangle")));
		entries.add(new UnlockEntry(4, SkillManager.instance.getSkill("ManaShield"), SkillManager.instance.getSkill("Shield"), SkillManager.instance.getSkill("Reflect"), SkillManager.instance.getSkill("LifeTap")));
		entries.add(new UnlockEntry(10, SkillManager.instance.getSkill("DiluteTime"), SkillManager.instance.getSkill("Freeze"), SkillManager.instance.getSkill("Slowfall"), SkillManager.instance.getSkill("Slow"), SkillManager.instance.getSkill("Weakness"), SkillManager.instance.getSkill("Lingering")));
		entries.add(new UnlockEntry(5, SkillManager.instance.getSkill("BuffPower"), SkillManager.instance.getSkill("Haste"), SkillManager.instance.getSkill("Slowfall"), SkillManager.instance.getSkill("SwiftSwim"), SkillManager.instance.getSkill("GravityWell"), SkillManager.instance.getSkill("Leap")));

		entries.add(new UnlockEntry(1, SkillManager.instance.getSkill("Daylight"), SkillManager.instance.getSkill("TrueSight"), SkillManager.instance.getSkill("DivineIntervention"), SkillManager.instance.getSkill("Light")));
		entries.add(new UnlockEntry(6, SkillManager.instance.getSkill("Moonrise"), SkillManager.instance.getSkill("NightVision"), SkillManager.instance.getSkill("EnderIntervention"), SkillManager.instance.getSkill("Lunar")));
		entries.add(new UnlockEntry(10, SkillManager.instance.getSkill("FortifyTime"), SkillManager.instance.getSkill("Shield"), SkillManager.instance.getSkill("ChronoAnchor"), SkillManager.instance.getSkill("Duration")));
		entries.add(new UnlockEntry(3, SkillManager.instance.getSkill("Prosperity"), SkillManager.instance.getSkill("Dig"), SkillManager.instance.getSkill("FeatherTouch"), SkillManager.instance.getSkill("MiningPower")));
	}

	static class UnlockEntry{
		private final ISkillTreeEntry unlock;
		private final ISkillTreeEntry[] requiredComponents;
		private final int guardian;

		public UnlockEntry(int guardian,ISkillTreeEntry unlock, ISkillTreeEntry... components){
			this.unlock = unlock;
			this.requiredComponents = components;
			this.guardian = guardian;
		}

		public boolean unlockIsInSpell(ItemStack spell){
			for (int i = 0; i < SpellUtils.instance.numStages(spell); ++i)
				if (partIsInStage(spell, unlock, i))
					return true;
			return false;
		}
		public boolean isGuardianKilled(EntityPlayer caster){
			if(!AMCore.config.GuardianKill()){
				return true;
			}
			boolean killed = false;
			ExtendedProperties props = ExtendedProperties.For(caster);
					switch(guardian){
					case 1 : if(props.guardian1) killed = true; break;
					case 2 : if(props.guardian2) killed = true; break;
					case 3 : if(props.guardian3) killed = true; break;
					case 4 : if(props.guardian4) killed = true; break;
					case 5 : if(props.guardian5) killed = true; break;
					case 6 : if(props.guardian6) killed = true; break;
					case 7 : if(props.guardian7) killed = true; break;
					case 8 : if(props.guardian8) killed = true; break;
					case 9 : if(props.guardian9) killed = true; break;
					case 10 : if(props.guardian10) killed = true; break;
					}
			return killed;
		}

		public boolean partIsInStage(ItemStack spell, ISkillTreeEntry part, int stage){
			if (part instanceof ISpellComponent && !SpellUtils.instance.componentIsPresent(spell, part.getClass(), stage)){
				return false;
			}else if (part instanceof ISpellModifier){
				for (SpellModifiers modifier : ((ISpellModifier)part).getAspectsModified()){
					if (!SpellUtils.instance.modifierIsPresent(modifier, spell, stage)){
						return false;
					}
				}
			}
			return true;
		}

		public boolean unlockIsInPrimaryTree(EntityPlayer caster){
			if (AMCore.config.getSkillTreeSecondaryTierCap() >= SkillTreeManager.instance.getHighestTier())
				return true;
			return SkillData.For(caster).getPrimaryTree() == SkillTreeManager.instance.getSkillTreeEntry(unlock).tree;
		}

		public boolean willSpellUnlock(ItemStack spell){
			for (int i = 0; i < SpellUtils.instance.numStages(spell); ++i){

				boolean found = true;

				for (ISkillTreeEntry part : requiredComponents){
					if (!partIsInStage(spell, part, i)){
						found = false;
						break;
					}
				}

				if (found)
					return true;
			}

			return false;
		}

		public void unlockFor(EntityPlayer player){
			if (!player.worldObj.isRemote){
				SkillData.For(player).incrementSpellPoints(SkillPointTypes.SILVER);
				SkillData.For(player).learn(SkillTreeManager.instance.getSkillTreeEntry(unlock).registeredItem);
				SkillData.For(player).forceSync();
			}
		}
	}
}
