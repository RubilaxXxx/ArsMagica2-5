package net.tclproject.mysteriumlib.asm.fixes;

import am2.AMCore;
import am2.AMEventHandler;
import am2.LogHelper;
import am2.affinity.AffinityHelper;
import am2.armor.BoundArmor;
import am2.blocks.liquid.BlockLiquidEssence;
import am2.buffs.BuffList;
import am2.entities.EntityHallucination;
import am2.entities.renderers.RenderPlayerSpecial;
import am2.items.*;
import am2.network.AMPacketProcessorClient;
import am2.network.TickrateMessage;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelMagmaCube;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;
import net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting;
import net.tclproject.mysteriumlib.asm.annotations.Fix;
import net.tclproject.mysteriumlib.asm.annotations.ReturnedValue;
import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import java.util.*;

import static am2.blocks.liquid.BlockLiquidEssence.liquidEssenceMaterial;

public class MysteriumPatchesFixesMagicka{

	public static List<int[]> providingRedstone = new ArrayList<int[]>();
	static int staffSlotTo = -1, staffSlotColumnTo = -1, staffSlotFrom = -1, staffSlotColumnFrom = -1;
	static int armorSlotTo = -1, armorSlotColumnTo = -1;
	static int spellSlotFrom = -1, spellSlotColumnFrom = -1;
	static boolean craftingStaffsPossible = false, craftingSpellsPossible = false, craftingArmorPossible = false;

	public static int countdownToChangeBack = -1;

	public static boolean isPlayerEthereal(EntityPlayer entityPlayer) {
		if (entityPlayer == null) return false;
		if (entityPlayer.inventory == null) return false;
		if (entityPlayer.inventory.armorInventory[0] != null) {
			if (ItemSoulspike.bootsHaveEtherealTag(entityPlayer.inventory.armorInventory[0])) {return true;}
		}
		return false;
	}

	// IEntitySelector impl. is useless because that'd make it incompatible in this case
	@Fix(returnSetting = EnumReturnSetting.ALWAYS, insertOnExit = true)
	public static List selectEntitiesWithinAABB(World wrld, Class p_82733_1_, AxisAlignedBB p_82733_2_, IEntitySelector p_82733_3_, @ReturnedValue List returnedValue)
	{
		ArrayList toReturn = new ArrayList();
		for (int i = 0; i < returnedValue.size(); i++) {
			if (i >= returnedValue.size()) break; // because we now know how volatile entity lists are.
			if (!(returnedValue.get(i) instanceof EntityPlayer)) {
				toReturn.add(returnedValue.get(i));
			} else { // if player
				if (!isPlayerEthereal((EntityPlayer)returnedValue.get(i))) toReturn.add(returnedValue.get(i));
			}
		}
		return toReturn;
	}

	@Fix(returnSetting = EnumReturnSetting.ALWAYS, insertOnExit = true)
	public static EntityPlayer getClosestPlayer(World world, double p_72977_1_, double p_72977_3_, double p_72977_5_, double p_72977_7_, @ReturnedValue EntityPlayer returnedValue)
	{
		return isPlayerEthereal(returnedValue) ? null : returnedValue;
	}

	// same as last method: prevent ethereal players from being returned
//	@Fix(returnSetting = EnumReturnSetting.ALWAYS, insertOnExit = true)
//	public static List getEntitiesWithinAABBExcludingEntity(World world, Entity p_94576_1_, AxisAlignedBB p_94576_2_, IEntitySelector p_94576_3_, @ReturnedValue List returnedValue)
//	{
//		ArrayList toReturn = new ArrayList();
//		for (int i = 0; i < returnedValue.size(); i++) {
//			if (i >= returnedValue.size()) break; // because we now know how volatile entity lists are.
//			if (!(returnedValue.get(i) instanceof EntityPlayer)) {
//				System.out.println("DEV TEST !");
//				toReturn.add(returnedValue.get(i));
//			} else { // if player
//				if (!isPlayerEthereal((EntityPlayer)returnedValue.get(i))) toReturn.add(returnedValue.get(i));
//			}
//		}
//		return toReturn;
//	}

	public static boolean updatingRenderWorld = false;
	public static boolean orientingCamera = false;

	@Fix
	public static void updateRenderer(WorldRenderer wr, EntityLivingBase p_147892_1_) { updatingRenderWorld = true; }

	@Fix(insertOnExit = true, targetMethod = "updateRenderer")
	public static void updateRendererEnd(WorldRenderer wr, EntityLivingBase p_147892_1_) { updatingRenderWorld = false; }

	@Fix
	public static void orientCamera(EntityRenderer er, float p_78467_1_) { orientingCamera = true; }

	@Fix(insertOnExit = true, targetMethod = "orientCamera")
	public static void orientCameraEnd(EntityRenderer er, float p_78467_1_) { orientingCamera = false; }

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static double distanceTo(Vec3 thisVec, Vec3 p_72438_1_)
	{
		if (FMLCommonHandler.instance() != null && FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
			if (!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
				if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
					if (isEtherealMinecraft()) return Double.MAX_VALUE;
				}
			} // just in case
		}
		double d0 = p_72438_1_.xCoord - thisVec.xCoord;
		double d1 = p_72438_1_.yCoord - thisVec.yCoord;
		double d2 = p_72438_1_.zCoord - thisVec.zCoord;
		return (double) MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
	}

	@SideOnly(Side.CLIENT) // method isn't clientside technically but we only need this patch clientside
	public static boolean isEtherealMinecraft() {
		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().renderViewEntity != null) {
			if (orientingCamera && isPlayerEthereal(Minecraft.getMinecraft().thePlayer) && Minecraft.getMinecraft().renderViewEntity == Minecraft.getMinecraft().thePlayer) return true;
		}
		return false;
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "renderPlayerSpecial")
	@SideOnly(Side.CLIENT)
	public static boolean getEntityRenderObject(RenderManager rm, Entity ent)
	{
		if (ent instanceof EntityPlayer) {
			if (playerModelMap.get(((EntityPlayer)ent).getCommandSenderName()) != null && playerModelMap.get(((EntityPlayer)ent).getCommandSenderName()).startsWith("maid")) return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static Render renderPlayerSpecial(RenderManager rm, Entity ent) {
		return new RenderPlayerSpecial();
	}

	// hacky fix for foamfix (foamfix, why must you do this?!)
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean register(EventBus eb, Object target)
	{
		if (target.getClass().getName().contains("Ears") && !AMCore.config.EnableEarsLoader()) {
			LogHelper.warn("--------------------------!!!--------------------------");
			LogHelper.warn("The Ears mod transformer was found and disabled. This most likely means you're using FoamFix's hacky 1.8+ skin support.");
			LogHelper.warn("Please disable it in FoamFix's config and use (preferably, as its solution is much more compatible and elegant) EtFuturumRequiem, or (if need be) SkinPort instead.");
			LogHelper.warn("--------------------------!!!--------------------------");
			return true; // Do not register the Ears transformer, for it is evil.
		}
		return false; // continue as normal
	}

	//	@cpw.mods.fml.common.Optional.Method(modid="FoamFixCore")
//	@Fix(returnSetting = EnumReturnSetting.ALWAYS) // foamfix compat doesn't work this way
//	public static boolean applyEarsPatch(pl.asie.foamfix.bugfixmod.coremod.BugfixModClassTransformer transformer) {
//		return false;
//	}

//	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
//	@SideOnly(Side.CLIENT)
//	public static boolean renderItemInFirstPerson(ItemRenderer ir, float p_78440_1_)
//	{
//		if (shouldNotUseNormalRender) return true;
//		return false;
//	}

	public static Map<String, String> playerModelMap = new HashMap<String, String>();

	@Fix(returnSetting = EnumReturnSetting.ALWAYS, insertOnExit = true)
	public static boolean isEntityInsideOpaqueBlock(Entity thisEntity, @ReturnedValue boolean returnedValue)
	{
		if (thisEntity.noClip) return false;
		return returnedValue;
	}

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static boolean canBePushed(EntityLivingBase elb)
	{
		if (elb instanceof EntityPlayer) {
			if (isPlayerEthereal((EntityPlayer) elb)) return false;
		}
		return !elb.isDead;
	}

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static boolean canBeCollidedWith(EntityLivingBase elb)
	{
		if (elb instanceof EntityPlayer) {
			if (isPlayerEthereal((EntityPlayer) elb)) return false;
		}
		return !elb.isDead;
	}

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static boolean isOnLadder(EntityLivingBase elb)
	{
		if (elb instanceof EntityPlayer) {
			if (isPlayerEthereal((EntityPlayer) elb)) return false;
		}
		return isOnLadderCalc(elb);
	}

	public static boolean isOnLadderCalc(EntityLivingBase elb)
	{
		int i = MathHelper.floor_double(elb.posX);
		int j = MathHelper.floor_double(elb.boundingBox.minY);
		int k = MathHelper.floor_double(elb.posZ);
		Block block = elb.worldObj.getBlock(i, j, k);
		return ForgeHooks.isLivingOnLadder(block, elb.worldObj, i, j, k, elb);
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "returnFalse")
	public static boolean handleWaterMovement(Entity thisEntity)
	{
		if (thisEntity instanceof EntityPlayer) {
			if (isPlayerEthereal((EntityPlayer) thisEntity)) return true; // return false from the original method
		}
		return false; // continue normal execution
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean processClickWindow(NetHandlerPlayServer nhps, C0EPacketClickWindow p_147351_1_) {
		if (isPlayerEthereal(nhps.playerEntity)) {
			nhps.playerEntity.func_143004_u();
			if (nhps.playerEntity.openContainer.windowId == p_147351_1_.func_149548_c() && nhps.playerEntity.openContainer.isPlayerNotUsingContainer(nhps.playerEntity)) {
				ArrayList<ItemStack> arraylist = new ArrayList<>();
				for (int i = 0; i < nhps.playerEntity.openContainer.inventorySlots.size(); ++i)
				{
					arraylist.add(((Slot)nhps.playerEntity.openContainer.inventorySlots.get(i)).getStack());
				}
				nhps.playerEntity.sendContainerAndContentsToPlayer(nhps.playerEntity.openContainer, arraylist);
				return true;
			}
		}
		return false;
	}

	public static boolean returnFalse(Entity thisEntity) { return false; }

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean applyEntityCollision(Entity thisEntity, Entity argumentPassed)
	{
		if (argumentPassed instanceof EntityPlayer) {
			if (isPlayerEthereal((EntityPlayer) argumentPassed)) return true; // stop execution
		}
		return false; // continue execution
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "returnMinusOne")
	public static boolean getRenderType(Block block) { // this is alright, we don't want to change overridden classes so allthatextends isn't necessary
//		if (updatingRenderWorld && isPlayerEthereal(Minecraft.getMinecraft().thePlayer)) return true;
		return false;
	}

//	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
//	@SideOnly(Side.CLIENT)
//	public static boolean playSound(SoundManager sm, ISound p_148611_1_)
//	{
//		if (Minecraft.getMinecraft() != null) {
//			if (Minecraft.getMinecraft().thePlayer != null && (AMPacketProcessorClient.deaf > 0)) return true;
//		}
//		return false; // play the sound
//	} // doesn't work because forge screwed up mappings

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	@SideOnly(Side.CLIENT)
	public static boolean renderParticles(EffectRenderer er, Entity p_78874_1_, float p_78874_2_)
	{
		if (Minecraft.getMinecraft() != null) {
			if (AMPacketProcessorClient.cloaking > 0) return true;
		}
		return false; // render as usual
	}

	public static int returnMinusOne(Block block) { return -1; }

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	@SideOnly(Side.CLIENT)
	public static boolean isInvisibleToPlayer(Entity e, EntityPlayer p_98034_1_){
		return (p_98034_1_.isPotionActive(BuffList.trueSight.id) || isPlayerEthereal(p_98034_1_)) ? false : e.isInvisible();
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean setDamage(Item item, ItemStack stack, int damage)
	{
		if (stack != null) {
			if (stack.getItem() instanceof ItemSoulspike && damage != 66 && damage != 0) return true;
		}
		return false;
	}

	// item handling: etherium (floating up) and cognitive dust (slowly floating up)
	@Fix
	public static void onUpdate(EntityItem ei)
	{
		if (ei.getEntityItem() != null) {
			if (ei.getEntityItem().getItem() instanceof ItemOre && ei.getEntityItem().getItemDamage() == ItemsCommonProxy.itemOre.META_COGNITIVEDUST) {
				ei.moveEntity(ei.motionX, ei.motionY, ei.motionZ);
				ei.motionY *= 0.0500000011920929D;
				ei.motionY += 0.025D;
			}
			if (ei.worldObj.isMaterialInBB(ei.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), liquidEssenceMaterial)) {
				double d0 = ei.posY;
				ei.moveEntity(ei.motionX, ei.motionY, ei.motionZ);
				ei.motionX *= 0.500000011920929D;
				ei.motionY *= 0.0500000011920929D;
				ei.motionZ *= 0.500000011920929D;
				ei.motionY += 0.05D;

				if (ei.isCollidedHorizontally && ei.isOffsetPositionInLiquid(ei.motionX, ei.motionY + 0.6000000238418579D - ei.posY + d0, ei.motionZ)) {
					ei.motionY = 0.30000001192092896D;
				}
				ei.fallDistance = 0;
			}
		}
	}

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static void switchToRealms(RealmsBridge rb, GuiScreen p_switchToRealms_1_)
	{
		return; // annoying realms crash disabler
	}

	private static Map<String, Integer> tileticks = new HashMap<>(); // for keeping track of ticks, not power

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static boolean hasWorldObj(TileEntity te) // very roundabout way to slow down tileentity ticks. The TE won't update if hasWorldObj in it returns false. it's only used in rendering chests otherwise, so there's no harm in overwriting this function.
	{
		if (te.getWorldObj() != null) {
			String thistile = te.xCoord + "_" + te.yCoord + "_" + te.zCoord + "_" + te.getWorldObj().provider.dimensionId;
			if (AMEventHandler.slowedTiles.containsKey(thistile)) {
				if (!tileticks.containsKey(thistile)) tileticks.put(thistile, 1);
				else tileticks.put(thistile, tileticks.get(thistile)+1);

				if (tileticks.get(thistile) > 1000) tileticks.put(thistile, 1);

				if (tileticks.get(thistile) % AMEventHandler.slowedTiles.get(thistile) != 0) {
					return false; // returns false from the original method
				}
			}
		}
		return te.getWorldObj() != null; // do not overwrite default behavior
	}

	@SideOnly(Side.CLIENT)
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean setLivingAnimations(ModelMagmaCube mmc, EntityLivingBase elb, float p_78086_2_, float p_78086_3_, float p_78086_4_)
	{
		if (elb instanceof EntityHallucination) return true;
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean setLivingAnimations(ModelSkeleton mmc, EntityLivingBase elb, float p_78086_2_, float p_78086_3_, float p_78086_4_)
	{
		if (elb instanceof EntityHallucination) return true;
		return false;
	}

	public static long servertickrate = 50L; // changing
	public static long servertickratedefault = 50L; // default

	public static float clienttickrate = 20; // stored in ticks as opposed to milis
	public static float clienttickratedefault = 20;

	// a few of the following methods are courtesy of Guichaguri (TickrateChanger mod)

	public static void changeTickrate(float ticksPerSecond) {
		if (AMCore.config.isGlobalTimeManipulationEnabled()) {
			changeServerTickrate(ticksPerSecond);
			changeClientTickratePublic(ticksPerSecond);
		}
	}

	public static void changeClientTickratePublic(float ticksPerSecond) {
		MinecraftServer server = MinecraftServer.getServer();
		if((server != null) && (server.getConfigurationManager() != null)) { // Is a server or singleplayer
			for(EntityPlayer p : (List<EntityPlayer>)server.getConfigurationManager().playerEntityList) {
				changeClientTickratePublic(p, ticksPerSecond);
			}
		} else { // Is in menu or a player connected in a server. We can say this is client.
			changeClientTickratePublic(null, ticksPerSecond);
		}
	}

	public static void changeClientTickratePublic(EntityPlayer player, float ticksPerSecond) {
		if((player == null) || (player.worldObj.isRemote)) { // Client
			if(FMLCommonHandler.instance().getSide() != Side.CLIENT) return;
			if((player != null) && (player != Minecraft.getMinecraft().thePlayer)) return;
			changeClientTickrate(ticksPerSecond);
		} else { // Server
			AMCore.NETWORK.sendTo(new TickrateMessage(ticksPerSecond), (EntityPlayerMP)player);
		}
	}

	private static final MethodHandle isDrawingGet = createIsDrawingGet();

	private static MethodHandle createIsDrawingGet() {
		try {
			Minecraft.getMinecraft(); // roundabout way to check for client
			return createIsDrawingGetDelegate();
		} catch (RuntimeException e) {
		} catch (NoClassDefFoundError e) {
		} catch (Exception e) {
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private static MethodHandle createIsDrawingGetDelegate() {
		try {
			Field field = CustomLoadingPlugin.isObfuscated() ? Tessellator.class.getDeclaredField("field_78415_z") : Tessellator.class.getDeclaredField("isDrawing");
			field.setAccessible(true);
			return MethodHandles.publicLookup().unreflectGetter(field);
		} catch (Exception e) {
			LogHelper.error("Did not find tesselator isDrawing field! Only report this error if you're seeing it on a client!");
			return null;
		}
	}

	// Completely removes the "already tessellating" error
	@SideOnly(Side.CLIENT)
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean startDrawing(Tessellator tsl, int p_78371_1_)
	{
		boolean isDrawing = false;
		try {
			isDrawing = (boolean) isDrawingGet.invokeExact((Tessellator) tsl);
		} catch (Throwable e) {
			throw new RuntimeException("Could not invoke isDrawing field! Only report this error if you're seeing it on a client!", e);
		}
		if (isDrawing)
		{
			return true;
		}
		return false;
	}

	// Completely removes the "already tessellating" error - part 2, electric boogaloo
	@SideOnly(Side.CLIENT)
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean draw(Tessellator tsl) {
		boolean isDrawing = false;
		try {
			isDrawing = (boolean) isDrawingGet.invokeExact((Tessellator) tsl);
		} catch (Throwable e) {
			throw new RuntimeException("Could not invoke isDrawing field! Only report this error if you're seeing it on a client!", e);
		}
		if (!isDrawing)
		{
			return true;
		}
		return false;
	}

	private static Field timerField = null;
	@SideOnly(Side.CLIENT)
	public static void changeClientTickrate(float newtickrate) {
		clienttickrate = newtickrate; // store it in case we need to access it later
		Minecraft mc = Minecraft.getMinecraft();
		if(mc == null) return;
		try {
			if(timerField == null) {
				for(Field fld : mc.getClass().getDeclaredFields()) {
					if(fld.getType() == net.minecraft.util.Timer.class) { // have to type out the whole class, otherwise it assumes it's the java.util timer
						timerField = fld;
						timerField.setAccessible(true);
						break;
					}
				}
			}
			timerField.set(mc, new net.minecraft.util.Timer(clienttickrate));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void changeServerTickrate(float newtickrate) {
		servertickrate = (long)(1000L / newtickrate); // 1000 milis in a second. 20 ticks a second. = 50 milis per tick.
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, booleanAlwaysReturned = true)
	public static boolean isBlockIndirectlyGettingPowered(World world, int x, int y, int z)
	{
		int theID = world.provider.dimensionId;
		boolean toReturn = false;
		int counter = 0;
		for (int[] redstoneProvider : providingRedstone) {
			if (redstoneProvider[0] == theID && redstoneProvider[1] == x && redstoneProvider[2] == y && redstoneProvider[3] == z) {
				toReturn = true;
				break;
			}
			counter++;
		}

		if (toReturn) {
			int newValue = providingRedstone.get(counter)[4] - 1;
			if (newValue <= 0) {
				providingRedstone.remove(counter);
			} else{
				providingRedstone.add(new int[]{theID, x, y, z, newValue});
				providingRedstone.remove(counter);
			}

			world.getBlock(x, y, z).onNeighborBlockChange(world, x, y, z, Blocks.stonebrick);
			return true;
		}
		return false;
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "isInsideWater")
	public static boolean isInsideOfMaterial(Entity e, Material p_70055_1_)
	{
		if (e instanceof EntityPlayer && p_70055_1_.isLiquid()) {
			if (AffinityHelper.isNotInWaterActually.contains((EntityPlayer)e)) {
				return true;
			}
		}
		return false;
	}

	@Fix
	public static void updateTick(BlockDynamicLiquid bdl, World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_){
		if (bdl.getMaterial() == Material.lava && p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_) instanceof BlockLiquidEssence) {
			p_149674_1_.setBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_, Blocks.stained_glass, 11, 3);
			func_149799_m(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_);
			return;
		}
	}

	protected static void func_149799_m(World p_149799_1_, int p_149799_2_, int p_149799_3_, int p_149799_4_) {
		p_149799_1_.playSoundEffect((double)((float)p_149799_2_ + 0.5F), (double)((float)p_149799_3_ + 0.5F), (double)((float)p_149799_4_ + 0.5F), "random.fizz", 0.5F, 2.6F + (p_149799_1_.rand.nextFloat() - p_149799_1_.rand.nextFloat()) * 0.8F);

		for (int l = 0; l < 8; ++l) {
			p_149799_1_.spawnParticle("largesmoke", (double)p_149799_2_ + Math.random(), (double)p_149799_3_ + 1.2D, (double)p_149799_4_ + Math.random(), 0.0D, 0.0D, 0.0D);
		}
	}

		@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "isInsideWater")
	public static boolean isInWater(Entity e) {
		if (e instanceof EntityPlayer) {
			if (AffinityHelper.isNotInWaterActually.contains((EntityPlayer)e)) {
				return true;
			}
		}
		return false;
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean velocityToAddToEntity(BlockLiquid block, World p_149640_1_, int p_149640_2_, int p_149640_3_, int p_149640_4_, Entity e, Vec3 p_149640_6_) {
		if (e instanceof EntityPlayer) {
			if (AffinityHelper.isNotInWaterActually.contains((EntityPlayer)e)) {
				return true;
			}
		}
		return false;
	}

	private static final ResourceLocation rodtexture1 = new ResourceLocation("arsmagica2", "textures/items/particles/particleswitharcanerod.png");
	private static final ResourceLocation rodtexture2 = new ResourceLocation("arsmagica2", "textures/items/particles/particleswithinfernalrod.png");

	@SideOnly(Side.CLIENT)
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "getEntityTextureResult")
	public static boolean getEntityTexture(RenderFish r, EntityFishHook e) {
		if (Minecraft.getMinecraft().thePlayer != null) {
			if (Minecraft.getMinecraft().thePlayer.getHeldItem() != null) {
				Item heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem().getItem();
				if (heldItem instanceof ItemArcaneFishingRod || heldItem instanceof ItemInfernalFishingRod) {
					return true;
				}
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT) // other methods of preventing flickering don't work
	@Fix(returnSetting = EnumReturnSetting.ON_TRUE)
	public static boolean doRenderShadowAndFire(Render r, Entity ent, double p_76979_2_, double p_76979_4_, double p_76979_6_, float p_76979_8_, float p_76979_9_)
	{
		if (ent instanceof EntityFishHook) return true;
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static ResourceLocation getEntityTextureResult(RenderFish r, EntityFishHook e) {
		if (Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemArcaneFishingRod) {
			return rodtexture1;
		} else {
			return rodtexture2;
		}
	}

	public static boolean isInsideWater(Entity e, Material p_70055_1_) {
		return false;
	}

	public static boolean isInsideWater(Entity e) {
		return false;
	}

	@Fix(returnSetting = EnumReturnSetting.ON_TRUE, anotherMethodReturned = "findMatchingRecipeResult")
	public static boolean findMatchingRecipe(CraftingManager cm, InventoryCrafting p_82787_1_, World p_82787_2_) {
		craftingStaffsPossible = false;
		craftingSpellsPossible = false;
		craftingArmorPossible = false;
		int craftingCompsStaffs = 0, craftingCompsSpells = 0, craftingCompsArmor = 0;
		for (int i = 0; i<3; i++) {
			for (int j = 0; j<3; j++) {
				if (p_82787_1_.getStackInRowAndColumn(i,j) != null) {
					if (p_82787_1_.getStackInRowAndColumn(i,j).getItem() instanceof ItemSpellStaff) {
						if (!((ItemSpellStaff)p_82787_1_.getStackInRowAndColumn(i,j).getItem()).isMagiTechStaff()) {
							craftingCompsStaffs++;
							if (staffSlotTo == -1) {
								staffSlotTo = i;
								staffSlotColumnTo = j;
							} else {
								staffSlotFrom = i;
								staffSlotColumnFrom = j;
							}
						}
					} else if (p_82787_1_.getStackInRowAndColumn(i,j).getItem() instanceof SpellBase) {
						craftingCompsSpells++;
						spellSlotFrom = i;
						spellSlotColumnFrom = j;
					} else if (p_82787_1_.getStackInRowAndColumn(i,j).getItem() instanceof BoundArmor) {
						craftingCompsArmor++;
						armorSlotTo = i;
						armorSlotColumnTo = j;
					}
				}
			}
		}

		if (craftingCompsSpells == 1 && craftingCompsStaffs == 1) {
			craftingSpellsPossible = true;
		} else if (craftingCompsStaffs == 2) {
			craftingStaffsPossible = true;
		} else if (craftingCompsSpells == 1 && craftingCompsArmor == 1) {
			craftingArmorPossible = true;
		}

		if (craftingStaffsPossible || craftingSpellsPossible || craftingArmorPossible) {
			return true;
		} else {
			staffSlotTo = -1;
			staffSlotColumnTo = -1;
			spellSlotFrom = -1;
			spellSlotColumnFrom = -1;
			staffSlotFrom = -1;
			staffSlotColumnFrom = -1;
			armorSlotColumnTo = -1;
			armorSlotTo = -1;
		}
		return false;
	}

	public static ItemStack findMatchingRecipeResult(CraftingManager cm, InventoryCrafting p_82787_1_, World p_82787_2_) {

		if (craftingStaffsPossible){
			ItemStack result = ItemSpellStaff.copyChargeFrom(
					p_82787_1_.getStackInRowAndColumn(staffSlotTo, staffSlotColumnTo).copy(),
					p_82787_1_.getStackInRowAndColumn(staffSlotFrom, staffSlotColumnFrom));
			staffSlotTo = -1;
			staffSlotFrom = -1;
			staffSlotColumnTo = -1;
			staffSlotColumnFrom = -1;
			return result;
		} else if (craftingSpellsPossible) {
			ItemStack result = ItemSpellStaff.setSpellScroll(
					p_82787_1_.getStackInRowAndColumn(staffSlotTo, staffSlotColumnTo).copy(),
					p_82787_1_.getStackInRowAndColumn(spellSlotFrom, spellSlotColumnFrom));
			staffSlotTo = -1;
			staffSlotColumnTo = -1;
			spellSlotFrom = -1;
			spellSlotColumnFrom = -1;
			return result;
		} else { // if crafting armor possible
			ItemStack result = BoundArmor.setSpell(
					p_82787_1_.getStackInRowAndColumn(armorSlotTo, armorSlotColumnTo).copy(),
					p_82787_1_.getStackInRowAndColumn(spellSlotFrom, spellSlotColumnFrom));
			armorSlotTo = -1;
			armorSlotColumnTo = -1;
			spellSlotFrom = -1;
			spellSlotColumnFrom = -1;
			return result;
		}
	}

}
