package am2.items;

import am2.AMCore;
import am2.api.math.AMVector3;
import am2.api.power.IBindable;
import am2.api.power.IManaPower;

import am2.blocks.tileentities.TileEntityCrystalMarker;
import am2.blocks.tileentities.TileEntityFlickerHabitat;
import am2.blocks.tileentities.TileEntityParticleEmitter;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;

import am2.texture.ResourceManager;
import cofh.lib.util.helpers.NBTHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;


public class ItemCrystalWrench extends ArsMagicaRotatedItem{

	@SideOnly(Side.CLIENT)
	private IIcon wrenchStoredIcon;

	@SideOnly(Side.CLIENT)
	private IIcon wrenchDisconnectIcon;

	private static final String KEY_PAIRLOC = "PAIRLOC";
	private static final String HAB_PAIRLOC = "HABLOC";
	private static final String KEEP_BINDING = "KEEPBINDING";
	private static final String MODE = "WRENCHMODE";
	private static final String BOUNDX = "boundx";
	private static final String BOUNDY = "boundy";
	private static final String BOUNDZ = "boundz";
	private  boolean storedbound = false;

	private static final boolean WRENCHMODE = true; //true to pair, false to disconnect


	public ItemCrystalWrench(){
		super();
		setMaxStackSize(1);
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = ResourceManager.RegisterTexture("crystal_wrench", par1IconRegister);
		wrenchStoredIcon = ResourceManager.RegisterTexture("crystal_wrench_stored", par1IconRegister);
		wrenchDisconnectIcon = ResourceManager.RegisterTexture("crystal_wrench_disconnect", par1IconRegister);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){


		TileEntity te = world.getTileEntity(x, y, z);
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		if (te != null && !(te instanceof IBindable || te instanceof TileEntityParticleEmitter) && !getMode(stack)){
			player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.wrongWrenchMode")));
			return true;
		}

		if (te instanceof IBindable){
			if(this.storedbound){
				// te => the tile entity we hit.
				//storedtile => the tile we have stored in the wand.
				ChunkCoordinates ChunkTile = GetStoredTile(stack);
				TileEntity storedTile = world.getTileEntity(ChunkTile.posX, ChunkTile.posY, ChunkTile.posZ);
				if (storedTile instanceof IBindable){
					if(storedTile == te){
						player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.nodePairToSelf")));
						return true;
					}
					if (!getMode(stack)){
						((IBindable)storedTile).unbind(world, ChunkTile.posX, ChunkTile.posY, ChunkTile.posZ);
						ClearStored(stack);
						player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.disconnectPower")));
						if(world.isRemote)
							spawnLinkParticles(player.worldObj, hitX, hitY, hitZ, true);
						return true;
					}

					if(((IBindable)storedTile).bindTo(world, te.xCoord, te.yCoord, te.zCoord, player)){
						player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am.tooltip.success")));
						ClearStored(stack);
						if(world.isRemote)
							spawnLinkParticles(player.worldObj, hitX, hitY, hitZ, false);
						return true;
					}
					System.out.print("CANT BIND!");
				}else return true;
			}
			else{
				System.out.print("STORING DATA !");
				storePairLocation(world, te, stack, player, x , y , z);
				return true;
			}
		}
		else if(player.isSneaking()){
			if(storedbound){
				System.out.print("CLEARING !");
				ClearStored(stack);
			}else{
				SetMode(stack);
			}
			return true;

		}
		else if (te instanceof TileEntityCrystalMarker && stack.stackTagCompound != null && stack.stackTagCompound.hasKey(HAB_PAIRLOC)){
			handleCMPair(stack, world, player, te, x + hitX, y + hitY, z + hitZ);
		}
		return true;
	}

	private void handleCMPair(ItemStack stack, World world, EntityPlayer player, TileEntity te, double hitX, double hitY, double hitZ){
		AMVector3 habLocation = AMVector3.readFromNBT(stack.stackTagCompound.getCompoundTag(HAB_PAIRLOC));
		if (world.isRemote){
			spawnLinkParticles(world, hitX, hitY, hitZ);
		}else{
			TileEntityCrystalMarker tecm = (TileEntityCrystalMarker)te;

			tecm.linkToHabitat(habLocation, player);

			if (!stack.stackTagCompound.hasKey(KEEP_BINDING))
				stack.stackTagCompound.removeTag(HAB_PAIRLOC);
		}
	}

	private void storePairLocation(World world, TileEntity te, ItemStack stack, EntityPlayer player, double hitX, double hitY, double hitZ){
//		AMVector3 destination = new AMVector3(te);
//
//			if (te instanceof TileEntityFlickerHabitat){
//				NBTTagCompound habLoc = new NBTTagCompound();
//				destination.writeToNBT(habLoc);
//				stack.stackTagCompound.setTag(HAB_PAIRLOC, habLoc);
//			}
//			else{
		System.out.print("Debug Stored tile V0 !");
				StoreTile(stack, te.xCoord,te.yCoord, te.zCoord);
//			}

		if(world.isRemote){
			spawnLinkParticles(world, hitX, hitY, hitZ);
		}
	}
	private ChunkCoordinates GetStoredTile(ItemStack stack){
		int x = stack.stackTagCompound.getInteger(BOUNDX);
		int y = stack.stackTagCompound.getInteger(BOUNDY);
		int z = stack.stackTagCompound.getInteger(BOUNDZ);
		return new ChunkCoordinates(x, y ,z);

	}
	private void StoreTile(ItemStack stack, int x, int y, int z){
		System.out.print("Debug Stored tile processing !");
		stack.stackTagCompound.setInteger(BOUNDX,x);
		stack.stackTagCompound.setInteger(BOUNDY,y);
		stack.stackTagCompound.setInteger(BOUNDZ,z);
		this.storedbound = true;
	}
	private void ClearStored(ItemStack stack){
		System.out.print("Debug Stored tile Clearing !");
		stack.stackTagCompound.removeTag(BOUNDX);
		stack.stackTagCompound.removeTag(BOUNDY);
		stack.stackTagCompound.removeTag(BOUNDZ);
		this.storedbound = false;
	}
	private void SetMode(ItemStack stack){
		if (getMode(stack))
			stack.stackTagCompound.setBoolean(MODE, !WRENCHMODE);
		else
			stack.stackTagCompound.setBoolean(MODE, WRENCHMODE);
	}
	public static boolean getMode(ItemStack stack){
			if (stack.stackTagCompound.hasKey(MODE)){
				return stack.stackTagCompound.getBoolean(MODE);
			}
			return true;
	}

	private void spawnLinkParticles(World world, double hitX, double hitY, double hitZ){
		spawnLinkParticles(world, hitX, hitY, hitZ, false);
	}

	public static void spawnLinkParticles(World world, double hitX, double hitY, double hitZ, boolean disconnect){
		for (int i = 0; i < 10; ++i){
			AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "none_hand", hitX, hitY, hitZ);
			if (particle != null){
				if (disconnect){
					particle.setRGBColorF(1, 0, 0);
					particle.addRandomOffset(0.5f, 0.5f, 0.5f);
				}
				particle.setMaxAge(10);
				particle.setParticleScale(0.1f);
				particle.AddParticleController(new ParticleMoveOnHeading(particle, world.rand.nextInt(360), world.rand.nextInt(360), world.rand.nextDouble() * 0.2, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.1f));
			}
		}
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass){
		return GetWrenchIcon(stack, pass);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining){
		return GetWrenchIcon(stack, renderPass);
	}

	@Override
	public IIcon getIconIndex(ItemStack par1ItemStack){
		return GetWrenchIcon(par1ItemStack, 0);
	}

	private IIcon GetWrenchIcon(ItemStack stack, int pass){
		if (stack.stackTagCompound != null && pass == 0){
			if (storedbound)
				return wrenchStoredIcon;
			else if (stack.stackTagCompound.hasKey(MODE) && !(stack.stackTagCompound.getBoolean(MODE)))
				return wrenchDisconnectIcon;
			else
				return this.itemIcon;
		}else{
			return this.itemIcon;
		}
	}
	private String GetModeString(ItemStack stack){
		if(stack.hasTagCompound())
			return "am2.tooltip." + (getMode(stack) ? "link" : "disconnect");
		return "am2.tooltip.link";
	}
	@Override
	public void addInformation(ItemStack stack, EntityPlayer p, List list, boolean adv){
		list.add(StatCollector.translateToLocal(GetModeString(stack)));
	}
}
/*
	private void doPairNodes(World world, int x, int y, int z, ItemStack stack, EntityPlayer player, double hitX, double hitY, double hitZ, TileEntity te){
		AMVector3 source = AMVector3.readFromNBT(stack.stackTagCompound.getCompoundTag(KEY_PAIRLOC));
		TileEntity sourceTE = world.getTileEntity((int)source.x, (int)source.y, (int)source.z);
		if (sourceTE instanceof IManaPower && !world.isRemote){
			player.addChatMessage(new ChatComponentText(PowerNodeRegistry.instance.tryPairNodes((IManaPower)sourceTE, (IManaPower)te)));
		}else if (world.isRemote){

		}
		if (!stack.stackTagCompound.hasKey(KEEP_BINDING))
			stack.stackTagCompound.removeTag(KEY_PAIRLOC);
	}

	private void doDisconnect(IManaPower node, World world, double hitX, double hitY, double hitZ, EntityPlayer player){
		PowerNodeRegistry.instance.tryDisconnectAllNodes(node);
		if (world.isRemote){
			spawnLinkParticles(player.worldObj, hitX, hitY, hitZ, true);
		}else{
			player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.disconnectPower")));
		}
	}
*/

//	private void SetKeepBinding(ItemStack stack){
//		if (stack.stackTagCompound.hasKey(KEEP_BINDING))
//			stack.stackTagCompound.removeTag(KEEP_BINDING);
//		else
//			stack.stackTagCompound.setBoolean(KEEP_BINDING, true);
//	}
