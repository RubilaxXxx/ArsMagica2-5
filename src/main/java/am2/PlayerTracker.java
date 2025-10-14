package am2;

import am2.armor.ArmorHelper;
import am2.armor.infusions.GenericImbuement;
import am2.customdata.CustomWorldData;
import am2.enchantments.AMEnchantments;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.playerextensions.AffinityData;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.RiftStorage;
import am2.playerextensions.SkillData;
import am2.proxy.tick.ServerTickHandler;
import am2.spell.SkillTreeManager;
import am2.utility.EntityUtilities;
import am2.utility.WebRequestUtils;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class PlayerTracker{

	public static HashMap<UUID, NBTTagCompound> storedExtProps_death;
	public static HashMap<UUID, NBTTagCompound> riftStorage_death;
	public static HashMap<UUID, NBTTagCompound> affinityStorage_death;
	public static HashMap<UUID, NBTTagCompound> spellKnowledgeStorage_death;

	public static HashMap<UUID, NBTTagCompound> storedExtProps_dimension;
	public static HashMap<UUID, NBTTagCompound> riftStorage_dimension;
	public static HashMap<UUID, NBTTagCompound> affinityStorage_dimension;
	public static HashMap<UUID, NBTTagCompound> spellKnowledgeStorage_dimension;


	public PlayerTracker(){
		storedExtProps_death = new HashMap<UUID, NBTTagCompound>();
		storedExtProps_dimension = new HashMap<UUID, NBTTagCompound>();
		affinityStorage_death = new HashMap<UUID, NBTTagCompound>();
		spellKnowledgeStorage_death = new HashMap<UUID, NBTTagCompound>();

		riftStorage_death = new HashMap<UUID, NBTTagCompound>();
		riftStorage_dimension = new HashMap<UUID, NBTTagCompound>();
		affinityStorage_dimension = new HashMap<UUID, NBTTagCompound>();
		spellKnowledgeStorage_dimension = new HashMap<UUID, NBTTagCompound>();

	}

	public void postInit(){
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event){
		if (hasAA(event.player)){
			AMNetHandler.INSTANCE.requestClientAuras((EntityPlayerMP)event.player);
		}

		int[] disabledSkills = SkillTreeManager.instance.getDisabledSkillIDs();

		AMDataWriter writer = new AMDataWriter();
		writer.add(AMCore.config.getSkillTreeSecondaryTierCap()).add(disabledSkills);
		writer.add(AMCore.config.getManaCap());
		byte[] data = writer.generate();

		CustomWorldData.syncAllWorldVarsToClients(event.player);

		AMNetHandler.INSTANCE.syncLoginData((EntityPlayerMP)event.player, data);
		if (ServerTickHandler.lastWorldName != null)
			AMNetHandler.INSTANCE.syncWorldName((EntityPlayerMP)event.player, ServerTickHandler.lastWorldName);
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event){
		//kill any summoned creatures
		if (!event.player.worldObj.isRemote){
			List list = event.player.worldObj.loadedEntityList;
			for (Object o : list){
				if (o instanceof EntityLivingBase && EntityUtilities.isSummon((EntityLivingBase)o) && EntityUtilities.getOwner((EntityLivingBase)o) == event.player.getEntityId()){
					((EntityLivingBase)o).setDead();
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event){
		//kill any summoned creatures, eventually respawn them in the new dimension
		if (!event.player.worldObj.isRemote){
			List list = event.player.worldObj.loadedEntityList;
			for (Object o : list){
				if (o instanceof EntityLivingBase && EntityUtilities.isSummon((EntityLivingBase)o) && EntityUtilities.getOwner((EntityLivingBase)o) == event.player.getEntityId()){
					((EntityLivingBase)o).setDead();
				}
			}
			ExtendedProperties.For(event.player).setDelayedSync(40);
			AffinityData.For(event.player).setDelayedSync(40);
			SkillData.For(event.player).setDelayedSync(40);
		}
	}



	public boolean hasAA(EntityPlayer entity){
		return getAAL(entity) > 0;
	}

	public int getAAL(EntityPlayer thePlayer){
		if (thePlayer == null) return 0;
		try{
			thePlayer.getDisplayName();
		}catch (Throwable t){
			return 0;
		}
		return thePlayer.getDisplayName().equalsIgnoreCase("Nlghtwing") ? 5 : 0;
	}
}
