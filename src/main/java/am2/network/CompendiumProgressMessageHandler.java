package am2.network;

import am2.AMCore;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToEntity;
import am2.particles.ParticleColorShift;
import am2.particles.ParticleHoldPosition;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItemFrame;

public class CompendiumProgressMessageHandler implements IMessageHandler<CompendiumProgressMessage, IMessage>{
	/**
	 * Called when a message is received of the appropriate type. You can optionally return a reply message, or null if no reply
	 * is needed.
	 *
	 * @param message The message
	 * @param ctx
	 * @return an optional return message
	 */
	@Override
	public IMessage onMessage(CompendiumProgressMessage message, MessageContext ctx){
		boolean complete = message.getComplete();
		EntityItemFrame frame = (EntityItemFrame)Minecraft.getMinecraft().theWorld.getEntityByID(message.getEntityid());

		if(complete) spawnCompendiumCompleteParticles(frame);
		else spawnCompendiumProgressParticles(frame, message.getX(), message.getY(), message.getZ());

		return null;
	}
	@SideOnly(Side.CLIENT)
	private static void spawnCompendiumProgressParticles(EntityItemFrame frame, int x, int y, int z){
		AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(frame.worldObj, "symbols", x + 0.5, y + 0.5, z + 0.5);
		if (particle != null){
			particle.setIgnoreMaxAge(true);
			//particle.AddParticleController(new ParticleApproachEntity(particle, frame, 0.02f, 0.04f, 1, false).setKillParticleOnFinish(true));
			particle.AddParticleController(new ParticleArcToEntity(particle, 1, frame, false).SetSpeed(0.02f).setKillParticleOnFinish(true));
			particle.setRandomScale(0.05f, 0.12f);
		}
	}

	@SideOnly(Side.CLIENT)
	private static void spawnCompendiumCompleteParticles(EntityItemFrame frame){
		AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(frame.worldObj, "radiant", frame.posX, frame.posY, frame.posZ);
		if (particle != null){
			particle.setIgnoreMaxAge(false);
			particle.setMaxAge(40);
			particle.setParticleScale(0.3f);
			//particle.AddParticleController(new ParticleApproachEntity(particle, frame, 0.02f, 0.04f, 1, false).setKillParticleOnFinish(true));
			particle.AddParticleController(new ParticleHoldPosition(particle, 40, 1, false));
			particle.AddParticleController(new ParticleColorShift(particle, 1, false).SetShiftSpeed(0.2f));
		}
	}


}
