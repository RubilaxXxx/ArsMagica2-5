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

import java.util.HashMap;
import java.util.List;
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
			AMNetHandler.INSTANCE.requestClientAuras((EntityPlayerMP)event.player);

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
			storeExtendedPropertiesForDimensionChange(event.player);
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

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event){
		//extended properties
		//================================================================================
		if (storedExtProps_death.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = storedExtProps_death.get(event.player.getUniqueID());
			storedExtProps_death.remove(event.player.getUniqueID());

			ExtendedProperties.For(event.player).loadNBTData(stored);
			ExtendedProperties.For(event.player).setDelayedSync(40);
		}else if (storedExtProps_dimension.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = storedExtProps_dimension.get(event.player.getUniqueID());
			storedExtProps_dimension.remove(event.player.getUniqueID());

			ExtendedProperties.For(event.player).loadNBTData(stored);
			ExtendedProperties.For(event.player).setDelayedSync(40);
		}
		//================================================================================
		//rift storage
		//================================================================================
		if (riftStorage_death.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = riftStorage_death.get(event.player.getUniqueID());
			riftStorage_death.remove(event.player.getUniqueID());

			RiftStorage.For(event.player).loadNBTData(stored);
		}else if (riftStorage_dimension.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = riftStorage_dimension.get(event.player.getUniqueID());
			riftStorage_dimension.remove(event.player.getUniqueID());

			RiftStorage.For(event.player).loadNBTData(stored);
		}
		//================================================================================
		//affinity data
		//================================================================================
		if (affinityStorage_death.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = affinityStorage_death.get(event.player.getUniqueID());
			affinityStorage_death.remove(event.player.getUniqueID());

			AffinityData.For(event.player).loadNBTData(stored);
		}else if (affinityStorage_dimension.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = affinityStorage_dimension.get(event.player.getUniqueID());
			affinityStorage_dimension.remove(event.player.getUniqueID());

			AffinityData.For(event.player).loadNBTData(stored);
		}
		//================================================================================
		//spell knowledge data
		//================================================================================
		if (spellKnowledgeStorage_death.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = spellKnowledgeStorage_death.get(event.player.getUniqueID());
			spellKnowledgeStorage_death.remove(event.player.getUniqueID());

			SkillData.For(event.player).loadNBTData(stored);
		}else if (spellKnowledgeStorage_dimension.containsKey(event.player.getUniqueID())){
			NBTTagCompound stored = spellKnowledgeStorage_dimension.get(event.player.getUniqueID());
			spellKnowledgeStorage_dimension.remove(event.player.getUniqueID());

			SkillData.For(event.player).loadNBTData(stored);
		}

	}

	public void onPlayerDeath(EntityPlayer player){
		storeExtendedPropertiesForRespawn(player);

	}

	public static void storeExtendedPropertiesForRespawn(EntityPlayer player){
		//extended properties
		//================================================================================
		if (storedExtProps_death.containsKey(player.getUniqueID()))
			storedExtProps_death.remove(player.getUniqueID());

		NBTTagCompound save = new NBTTagCompound();
		ExtendedProperties.For(player).saveNBTData(save);

		storedExtProps_death.put(player.getUniqueID(), save);

		//================================================================================
		//rift storage
		//================================================================================
		if (riftStorage_death.containsKey(player.getUniqueID()))
			riftStorage_death.remove(player.getUniqueID());
		NBTTagCompound saveRift = new NBTTagCompound();
		RiftStorage.For(player).saveNBTData(saveRift);

		riftStorage_death.put(player.getUniqueID(), saveRift);

		//================================================================================
		//affinity storage
		//================================================================================
		if (affinityStorage_death.containsKey(player.getUniqueID()))
			affinityStorage_death.remove(player.getUniqueID());
		NBTTagCompound saveAffinity = new NBTTagCompound();
		AffinityData.For(player).saveNBTData(saveAffinity);

		affinityStorage_death.put(player.getUniqueID(), saveAffinity);
		//================================================================================
		//affinity storage
		//================================================================================
		if (spellKnowledgeStorage_death.containsKey(player.getUniqueID()))
			spellKnowledgeStorage_death.remove(player.getUniqueID());
		NBTTagCompound saveSpellKnowledge = new NBTTagCompound();
		SkillData.For(player).saveNBTData(saveSpellKnowledge);

		spellKnowledgeStorage_death.put(player.getUniqueID(), saveSpellKnowledge);
		//================================================================================
	}

	public static void storeExtendedPropertiesForDimensionChange(EntityPlayer player){
		//extended properties
		//================================================================================
		if (!storedExtProps_death.containsKey(player.getUniqueID())){
			if (storedExtProps_dimension.containsKey(player.getUniqueID()))
				storedExtProps_dimension.remove(player.getUniqueID());

			NBTTagCompound saveExprop = new NBTTagCompound();
			ExtendedProperties.For(player).saveNBTData(saveExprop);

			storedExtProps_dimension.put(player.getUniqueID(), saveExprop);
		}
		//================================================================================
		//rift storage
		//================================================================================
		if (!riftStorage_death.containsKey(player.getUniqueID())){
			if (riftStorage_dimension.containsKey(player.getUniqueID()))
				riftStorage_dimension.remove(player.getUniqueID());

			NBTTagCompound saveRift = new NBTTagCompound();
			RiftStorage.For(player).saveNBTData(saveRift);

			riftStorage_dimension.put(player.getUniqueID(), saveRift);
		}
		//================================================================================
		//affinity storage
		//================================================================================
		if (!affinityStorage_death.containsKey(player.getUniqueID())){
			if (affinityStorage_dimension.containsKey(player.getUniqueID()))
				affinityStorage_dimension.remove(player.getUniqueID());

			NBTTagCompound saveAffinity = new NBTTagCompound();
			AffinityData.For(player).saveNBTData(saveAffinity);

			affinityStorage_dimension.put(player.getUniqueID(), saveAffinity);
		}
		//================================================================================
		//spell knowledge storage
		//================================================================================
		if (!spellKnowledgeStorage_death.containsKey(player.getUniqueID())){
			if (spellKnowledgeStorage_dimension.containsKey(player.getUniqueID()))
				spellKnowledgeStorage_dimension.remove(player.getUniqueID());

			NBTTagCompound spellKnowledge = new NBTTagCompound();
			SkillData.For(player).saveNBTData(spellKnowledge);

			spellKnowledgeStorage_dimension.put(player.getUniqueID(), spellKnowledge);
		}
		//================================================================================
	}
}
