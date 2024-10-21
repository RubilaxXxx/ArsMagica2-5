package am2.bosses.ai;

import am2.api.entities.Bosses.BossActionsAPI;
import am2.bosses.EntityWaterGuardian;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAICloneSelf extends EntityAIBase{
	private final EntityWaterGuardian host;
	private int cooldownTicks = 0;

	public EntityAICloneSelf(EntityWaterGuardian host){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		if (cooldownTicks-- > 0 || host.getCurrentAction() != BossActionsAPI.IDLE || !host.isActionValid(BossActionsAPI.CLONE))
			return false;
		EntityLivingBase AITarget = host.getAttackTarget();
		if (AITarget == null || AITarget.isDead) return false;
		return true;
	}

	@Override
	public boolean continueExecuting(){
		if (host.getCurrentAction() == BossActionsAPI.CLONE && host.getTicksInCurrentAction() > host.getCurrentAction().getMaxActionTime()){
			host.setCurrentAction(BossActionsAPI.IDLE);
			cooldownTicks = 200;
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		if (host.getCurrentAction() != BossActionsAPI.CLONE)
			host.setCurrentAction(BossActionsAPI.CLONE);

		if (!host.worldObj.isRemote && host.getCurrentAction() == BossActionsAPI.CLONE && host.getTicksInCurrentAction() == 30){
			EntityWaterGuardian clone1 = spawnClone();
			EntityWaterGuardian clone2 = spawnClone();

			host.setClones(clone1, clone2);
		}
	}

	private EntityWaterGuardian spawnClone(){
		EntityWaterGuardian clone = new EntityWaterGuardian(host.worldObj);
		clone.setMaster(host);
		clone.setPosition(host.posX, host.posY, host.posZ);
		host.worldObj.spawnEntityInWorld(clone);
		return clone;
	}
}
