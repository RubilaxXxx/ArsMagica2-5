package am2;

import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import am2.armor.AMArmor;
import am2.armor.ArmorHelper;
import am2.armor.infusions.GenericImbuement;
import am2.blocks.BlockCrystalMarker;
import am2.blocks.BlocksCommonProxy;
import am2.blocks.tileentities.TileEntityCrystalMarker;
import am2.blocks.tileentities.TileEntityParticleEmitter;
import am2.buffs.BuffList;
import am2.entities.EntityShadowHelper;
import am2.guis.AMGuiHelper;
import am2.items.ItemsCommonProxy;
import am2.lore.ArcaneCompendium;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import am2.playerextensions.ExtendedProperties;
import am2.power.PowerNodeEntry;
import am2.utility.EntityUtilities;
import am2.utility.RenderUtilities;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesMagicka;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AMClientEventHandler{
	@SubscribeEvent
	public void onBlockHighlight(DrawBlockHighlightEvent event){

		if (AMCore.proxy.getLocalPlayer().getCurrentArmor(3) != null &&
				(AMCore.proxy.getLocalPlayer().getCurrentArmor(3).getItem() == ItemsCommonProxy.magitechGoggles ||
						ArmorHelper.isInfusionPreset(AMCore.proxy.getLocalPlayer().getCurrentArmor(3), GenericImbuement.magitechGoggleIntegration))
				){

			TileEntity te = event.player.worldObj.getTileEntity(event.target.blockX, event.target.blockY, event.target.blockZ);
			if (te != null && te instanceof IPowerNode){
				AMCore.proxy.setTrackedLocation(new AMVector3(event.target.blockX, event.target.blockY, event.target.blockZ));
			}else{
				AMCore.proxy.setTrackedLocation(AMVector3.zero());
			}

			if (AMCore.proxy.hasTrackedLocationSynced()){
				renderPowerFloatingText(event, te);
			}

			if (te instanceof TileEntityCrystalMarker){
				renderPriorityText(event, (TileEntityCrystalMarker)te);
			}
		}
	}

	private void renderPowerFloatingText(DrawBlockHighlightEvent event, TileEntity te){
		PowerNodeEntry data = AMCore.proxy.getTrackedData();
		Block block = event.player.worldObj.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ);
		float yOff = 0.5f;
		if (data != null){
			GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
			for (PowerTypes type : ((IPowerNode)te).getValidPowerTypes()){
				float pwr = data.getPower(type);
				float pct = pwr / ((IPowerNode)te).getCapacity() * 100;
				AMVector3 offset = new AMVector3(event.target.blockX + 0.5, event.target.blockY + 0.5, event.target.blockZ + 0.5).sub(
						new AMVector3((event.player.prevPosX - (event.player.prevPosX - event.player.posX) * event.partialTicks),
								(event.player.prevPosY - (event.player.prevPosY - event.player.posY) * event.partialTicks) + event.player.getEyeHeight(),
								(event.player.prevPosZ - (event.player.prevPosZ - event.player.posZ) * event.partialTicks)));
				offset = offset.normalize();
				if (event.target.blockY <= event.player.posY - 0.5){
					RenderUtilities.drawTextInWorldAtOffset(String.format("%s%.2f (%.2f%%)", type.chatColor(), pwr, pct),
							event.target.blockX - (event.player.prevPosX - (event.player.prevPosX - event.player.posX) * event.partialTicks) + 0.5f - offset.x,
							event.target.blockY + yOff - (event.player.prevPosY - (event.player.prevPosY - event.player.posY) * event.partialTicks) + block.getBlockBoundsMaxY() * 0.8f,
							event.target.blockZ - (event.player.prevPosZ - (event.player.prevPosZ - event.player.posZ) * event.partialTicks) + 0.5f - offset.z,
							0xFFFFFF);
					yOff += 0.12f;
				}else{
					RenderUtilities.drawTextInWorldAtOffset(String.format("%s%.2f (%.2f%%)", type.chatColor(), pwr, pct),
							event.target.blockX - (event.player.prevPosX - (event.player.prevPosX - event.player.posX) * event.partialTicks) + 0.5f - offset.x,
							event.target.blockY - yOff - (event.player.prevPosY - (event.player.prevPosY - event.player.posY) * event.partialTicks) - block.getBlockBoundsMaxY() * 0.2f,
							event.target.blockZ - (event.player.prevPosZ - (event.player.prevPosZ - event.player.posZ) * event.partialTicks) + 0.5f - offset.z,
							0xFFFFFF);
					yOff -= 0.12f;
				}
			}
			GL11.glPopAttrib();
		}
	}

	private void renderPriorityText(DrawBlockHighlightEvent event, TileEntityCrystalMarker te){
		int meta = event.player.worldObj.getBlockMetadata(event.target.blockX, event.target.blockY, event.target.blockZ);
		if (meta == BlockCrystalMarker.META_IN) //no priority for these blocks
			return;

		Block block = event.player.worldObj.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ);
		float yOff = 0.5f;
		String priString = String.format(StatCollector.translateToLocal("am2.tooltip.priority"), String.format("%d", te.getPriority()));

		AMVector3 boundsOffset = new AMVector3(
				block.getBlockBoundsMinX() + (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX()) / 2,
				block.getBlockBoundsMinY() + (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY()) / 2,
				block.getBlockBoundsMinZ() + (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ()) / 2);

		AMVector3 offset = new AMVector3(
				event.target.blockX + boundsOffset.x,
				event.target.blockY + boundsOffset.y,
				event.target.blockZ + boundsOffset.z
		).sub(new AMVector3(
						(event.player.prevPosX - (event.player.prevPosX - event.player.posX) * event.partialTicks),
						(event.player.prevPosY - (event.player.prevPosY - event.player.posY) * event.partialTicks) + event.player.getEyeHeight(),
						(event.player.prevPosZ - (event.player.prevPosZ - event.player.posZ) * event.partialTicks)
				)
		);

		offset = offset.normalize();
		AMVector3 drawPos = new AMVector3(
				event.target.blockX + block.getBlockBoundsMaxX() - offset.x - (event.player.prevPosX - (event.player.prevPosX - event.player.posX) * event.partialTicks),
				event.target.blockY + yOff - (event.player.prevPosY - (event.player.prevPosY - event.player.posY) * event.partialTicks) + block.getBlockBoundsMaxY() * 0.8f,
				event.target.blockZ + block.getBlockBoundsMaxZ() - offset.z - (event.player.prevPosZ - (event.player.prevPosZ - event.player.posZ) * event.partialTicks)
		);
		if (event.target.blockY > event.player.posY - 0.5){
			drawPos.y = (float)(event.target.blockY - yOff - (event.player.prevPosY - (event.player.prevPosY - event.player.posY) * event.partialTicks) - block.getBlockBoundsMaxY() * 0.2f);
		}

		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
		RenderUtilities.drawTextInWorldAtOffset(priString, drawPos.x, drawPos.y, drawPos.z, 0xFFFFFF);
		GL11.glPopAttrib();
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event){
		ItemStack stack = event.itemStack;
		if (stack != null && stack.getItem() instanceof ItemArmor){
			double xp = 0;
			int armorLevel = 0;
			String[] effects = new String[0];

			if (stack.hasTagCompound()){
				NBTTagCompound armorCompound = (NBTTagCompound)stack.stackTagCompound.getTag(AMArmor.NBT_KEY_AMPROPS);
				if (armorCompound != null){
					xp = armorCompound.getDouble(AMArmor.NBT_KEY_TOTALXP);
					armorLevel = armorCompound.getInteger(AMArmor.NBT_KEY_ARMORLEVEL);
					String effectsList = armorCompound.getString(AMArmor.NBT_KEY_EFFECTS);
					if (effectsList != null && effectsList != ""){
						effects = effectsList.split(AMArmor.INFUSION_DELIMITER);
					}
				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
				event.toolTip.add(StatCollector.translateToLocalFormatted("am2.tooltip.armorxp", String.format("%.2f", xp)));
				event.toolTip.add(String.format(StatCollector.translateToLocal("am2.tooltip.armorlevel"), armorLevel));
				for (String s : effects){
					event.toolTip.add(StatCollector.translateToLocal("am2.tooltip." + s));
				}

			}else{
				event.toolTip.add(StatCollector.translateToLocal("am2.tooltip.shiftForDetails"));
			}
		}else if (stack.getItem() instanceof ItemBlock){
			if (((ItemBlock)stack.getItem()).field_150939_a == BlocksCommonProxy.manaBattery){
				if (stack.hasTagCompound()){
					float batteryCharge = stack.stackTagCompound.getFloat("power_charge");
					PowerTypes powerType = PowerTypes.getByID(stack.stackTagCompound.getInteger("outputType"));
					if (batteryCharge != 0) {
						// TODO localize this tooltip
						event.toolTip.add(String.format("\u00A7r\u00A79Contains \u00A75%.2f %s%s \u00A79etherium", batteryCharge, powerType.chatColor(), powerType.name()));
					}
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMouseEvent(MouseEvent event){
		event.setCanceled(AMCore.proxy.setMouseDWheel(event.dwheel));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiRender(RenderGameOverlayEvent event){
		if (event.type == ElementType.CROSSHAIRS || event.type == ElementType.TEXT)
			AMCore.proxy.renderGameOverlay();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerRender(RenderPlayerEvent.Pre event){
		double dX = AMCore.proxy.getLocalPlayer().posX - event.entityPlayer.posX;
		double dY = AMCore.proxy.getLocalPlayer().posY - event.entityPlayer.posY;
		double dZ = AMCore.proxy.getLocalPlayer().posZ - event.entityPlayer.posZ;

		double dpX = AMCore.proxy.getLocalPlayer().prevPosX - event.entityPlayer.prevPosX;
		double dpY = AMCore.proxy.getLocalPlayer().prevPosY - event.entityPlayer.prevPosY;
		double dpZ = AMCore.proxy.getLocalPlayer().prevPosZ - event.entityPlayer.prevPosZ;

		double transX = dpX + (dX - dpX) * event.partialRenderTick;
		double transY = dpY + (dY - dpY) * event.partialRenderTick;
		double transZ = dpZ + (dZ - dpZ) * event.partialRenderTick;

		if (ExtendedProperties.For(event.entityPlayer).getFlipRotation() > 0){
			GL11.glPushMatrix();

			GL11.glTranslated(-transX, -transY, -transZ);
			GL11.glRotatef(ExtendedProperties.For(event.entityPlayer).getFlipRotation(), 0, 0, 1.0f);
			GL11.glTranslated(transX, transY, transZ);

			float offset = event.entityPlayer.height * (ExtendedProperties.For(event.entityPlayer).getFlipRotation() / 180.0f);
			GL11.glTranslatef(0, -offset, 0);
		}

		float shrink = ExtendedProperties.For(event.entityPlayer).getShrinkPct();
		if (shrink > 0){
			GL11.glPushMatrix();
			GL11.glTranslatef(0, 0 - 0.5f * shrink, 0);
			GL11.glScalef(1 - 0.5f * shrink, 1 - 0.5f * shrink, 1 - 0.5f * shrink);
		}

	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerRender(RenderPlayerEvent.Post event){
		ModelBiped mainModel = ReflectionHelper.getPrivateValue(RenderPlayer.class, event.renderer, "field_77109_a", "modelBipedMain");
		if (mainModel != null){
			mainModel.bipedLeftArm.isHidden = false;
			mainModel.bipedRightArm.isHidden = false;
		}

		if (ExtendedProperties.For(event.entityPlayer).getFlipRotation() > 0){
			GL11.glPopMatrix();
		}
		if (ExtendedProperties.For(event.entityPlayer).getShrinkPct() > 0){
			GL11.glPopMatrix();
		}

		if (event.entityPlayer == AMCore.proxy.getLocalPlayer()){
			if (AMCore.proxy.getLocalPlayer().isPotionActive(BuffList.trueSight.id)){
				if (AMGuiHelper.instance.playerRunesAlpha < 1)
					AMGuiHelper.instance.playerRunesAlpha += 0.01f;
			}else{
				if (AMGuiHelper.instance.playerRunesAlpha > 0)
					AMGuiHelper.instance.playerRunesAlpha -= 0.01f;
			}


			if (AMGuiHelper.instance.playerRunesAlpha > 0){
				int runeCombo = EntityUtilities.getRuneCombo(event.entityPlayer);
				int numRunes = 0;
				for (int i = 0; i <= 16; ++i){
					int bit = 1 << i;
					if ((runeCombo & bit) == bit){
						numRunes++;
					}
				}
				double step = 0.25f;
				double xOffset = -(numRunes / 2.0f) * step + ((numRunes % 2 == 0) ? step / 2f : 0);
				for (int i = 0; i <= 16; ++i){
					int bit = 1 << i;
					if ((runeCombo & bit) == bit){
						RenderUtilities.DrawIconInWorldAtOffset(ItemsCommonProxy.rune.getIconFromDamage(i), xOffset, 0.5f, 0, 0.25f, 0.25f);
						xOffset += step;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if (event.entity instanceof EntityShadowHelper){
			((EntityShadowHelper)event.entity).onJoinWorld(event.world);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onEntityJoinWorldClient(EntityJoinWorldEvent event){
		if (event.entity instanceof EntityPlayer) {
			if (event.entity == Minecraft.getMinecraft().thePlayer) ArcaneCompendium.instance.loadUnlockData();
		}
		if (event.entity instanceof EntityPlayer) {
			if (event.entity != Minecraft.getMinecraft().thePlayer) {
				AMDataWriter writer = new AMDataWriter();
				writer.add(MysteriumPatchesFixesMagicka.playerModelMap.size());
				for (Map.Entry<String, String> entry : MysteriumPatchesFixesMagicka.playerModelMap.entrySet()) {
					writer.add(entry.getKey());
					writer.add(entry.getValue());
				}
				AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.SYNCMAPTOSERVER, writer.generate());
				return;
				// syncs the map to everyone, and as a consequence, the new player too
			}
			ServerData serverData = Minecraft.getMinecraft().func_147104_D();
			if (serverData == null) {
				MysteriumPatchesFixesMagicka.playerModelMap.clear();
				return; // singleplayer
			}

			List<String> tablist = ((List<GuiPlayerInfo>) Minecraft.getMinecraft().thePlayer.sendQueue.playerInfoList).stream().map(r -> r.name).collect(Collectors.toList());
			if (tablist.contains(Minecraft.getMinecraft().thePlayer.getDisplayName())) tablist.remove(Minecraft.getMinecraft().thePlayer.getDisplayName());
			if (tablist.contains(Minecraft.getMinecraft().thePlayer.getCommandSenderName())) tablist.remove(Minecraft.getMinecraft().thePlayer.getCommandSenderName());
			if (event.entity == Minecraft.getMinecraft().thePlayer && tablist.size() == 0) { // no players to sync the map from, clear it instead
				MysteriumPatchesFixesMagicka.playerModelMap.clear();
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.face == -1){
			return;
		}
		
		Block block = event.world.getBlock(event.x + Facing.offsetsXForSide[event.face], event.y + Facing.offsetsYForSide[event.face], event.z + Facing.offsetsZForSide[event.face]);
		
		if (block == BlocksCommonProxy.particleEmitter){
			TileEntity te = event.world.getTileEntity(event.x + Facing.offsetsXForSide[event.face], event.y + Facing.offsetsYForSide[event.face], event.z + Facing.offsetsZForSide[event.face]);
			
			if (te != null && te instanceof TileEntityParticleEmitter && !((TileEntityParticleEmitter)te).getShow()){
				event.setCanceled(true);
		  }
		}
	}
}
