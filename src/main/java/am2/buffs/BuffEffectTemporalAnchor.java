package am2.buffs;

import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class BuffEffectTemporalAnchor extends BuffEffect{

	private double x;
	private double y;
	private double z;
	private float rotationPitch;
	private float rotationYaw;
	private float rotationYawHead;

	private float mana;
	private float health;

	public BuffEffectTemporalAnchor(int duration, int amplifier){
		super(BuffList.temporalAnchor.id, duration, amplifier);
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving){
		//store values from the entity
		x = entityliving.posX;
		y = entityliving.posY;
		z = entityliving.posZ;

		rotationPitch = entityliving.rotationPitch;
		rotationYaw = entityliving.rotationYaw;
		rotationYawHead = entityliving.rotationYawHead;

		health = entityliving.getHealth();
		mana = ExtendedProperties.For(entityliving).getCurrentMana();
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){
		if (health >= 1) {
			entityliving.setPositionAndUpdate(x, y, z);
			entityliving.rotationYawHead = rotationYawHead;
			entityliving.rotationPitch = rotationPitch;
			entityliving.rotationYaw = rotationYaw;
			ExtendedProperties.For(entityliving).setCurrentMana(mana);
			entityliving.setHealth(Math.max(1, health));
			entityliving.addPotionEffect(new BuffEffectTemporalAnchorBurnout(1800, 0));
			if (health == 0)// to only explicitly fix level data resets
				entityliving.attackEntityFrom(DamageSource.outOfWorld, 5000); // still kills but avoids broken setHealth() call
			entityliving.fallDistance = 0;
		}
	}

	@Override
	protected String spellBuffName(){
		return "Temporal Anchor";
	}

}
