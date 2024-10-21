package am2.bosses.ai;

import am2.bosses.EntityFireGuardian;
import am2.api.entities.Bosses.BossActionsAPI;
import am2.api.entities.Bosses.IArsMagicaBoss;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIDive extends EntityAIBase{

	private final EntityFireGuardian host;
	private int cooldownTicks = 0;

	public EntityAIDive(EntityFireGuardian host){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		boolean execute = ((IArsMagicaBoss)host).getCurrentAction() == BossActionsAPI.IDLE && host.getAttackTarget() != null && cooldownTicks-- <= 0;
		if (execute)
			((IArsMagicaBoss)host).setCurrentAction(BossActionsAPI.SPINNING);
		return execute;
	}

	@Override
	public boolean continueExecuting(){
		if (host.getAttackTarget() == null || host.getAttackTarget().isDead || host.getNumHits() >= 3){
			this.cooldownTicks = 300;
			((IArsMagicaBoss)host).setCurrentAction(BossActionsAPI.IDLE);
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		host.getNavigator().tryMoveToEntityLiving(host.getAttackTarget(), 0.75f);
		if (((IArsMagicaBoss)host).getTicksInCurrentAction() > 40 && host.getDistanceSqToEntity(host.getAttackTarget()) < 64D){
			((IArsMagicaBoss)host).setCurrentAction(BossActionsAPI.SPINNING);
		}
	}

}
