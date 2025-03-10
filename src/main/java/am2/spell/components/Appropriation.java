package am2.spell.components;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemSpellBook;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitPoint;
import am2.spell.SpellUtils;
import am2.utility.DummyEntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class Appropriation implements ISpellComponent{

	private static final String storageKey = "stored_data";
	private static final String storageType = "storage_type";

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				Items.ender_pearl,
				new ItemStack(BlocksCommonProxy.AMOres, 1, BlocksCommonProxy.AMOres.META_CHIMERITE_BLOCK),
				Blocks.chest
		};
	}

	@Override
	public int getID(){
		return 71;
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){

		if (!(caster instanceof EntityPlayer))
			return false;

		ItemStack originalSpellStack = getOriginalSpellStack((EntityPlayer)caster);
		if (originalSpellStack == null){
			return false;
		}

		if (originalSpellStack.stackTagCompound == null){
			return false;
		}

		Block block = world.getBlock(blockx, blocky, blockz);

		if (block == null){
			return false;
		}

		for (String s : AMCore.config.getAppropriationBlockBlacklist()){
			if (block.getUnlocalizedName() == s){
				return false;
			}
		}

		if (!world.isRemote){
			if (originalSpellStack.stackTagCompound.hasKey(storageKey)){

				if (world.getBlock(blockx, blocky, blockz) == Blocks.air) blockFace = -1;
				if (blockFace != -1){
					switch (blockFace){
					case 0:
						blocky--;
						break;
					case 1:
						blocky++;
						break;
					case 2:
						blockz--;
						break;
					case 3:
						blockz++;
						break;
					case 4:
						blockx--;
						break;
					case 5:
						blockx++;
						break;
					}
				}

				if (world.isAirBlock(blockx, blocky, blockz) || !world.getBlock(blockx, blocky, blockz).getMaterial().isSolid()){

					// save current spell
					NBTTagCompound nbt = null;
					if (stack.getTagCompound() != null){
						nbt = (NBTTagCompound)stack.getTagCompound().copy();
					}


					EntityPlayerMP casterPlayer = (EntityPlayerMP)DummyEntityPlayer.fromEntityLiving(caster);
					world.captureBlockSnapshots = true;
					restore((EntityPlayer)caster, world, originalSpellStack, blockx, blocky, blockz, impactX, impactY, impactZ);
					world.captureBlockSnapshots = false;

					// save new spell data
					NBTTagCompound newNBT = null;
					if (stack.getTagCompound() != null){
						newNBT = (NBTTagCompound)stack.getTagCompound().copy();
					}

					net.minecraftforge.event.world.BlockEvent.PlaceEvent placeEvent = null;
					List<net.minecraftforge.common.util.BlockSnapshot> blockSnapshots = (List<net.minecraftforge.common.util.BlockSnapshot>) world.capturedBlockSnapshots.clone();
					world.capturedBlockSnapshots.clear();

					// restore original item data for event
					if (nbt != null){
						stack.setTagCompound(nbt);
					}
					if (blockSnapshots.size() > 1){
						placeEvent = ForgeEventFactory.onPlayerMultiBlockPlace(casterPlayer, blockSnapshots, ForgeDirection.UNKNOWN);
					} else if (blockSnapshots.size() == 1){
						placeEvent = ForgeEventFactory.onPlayerBlockPlace(casterPlayer, blockSnapshots.get(0), ForgeDirection.UNKNOWN);
					}

					if (placeEvent != null && (placeEvent.isCanceled())){
						// revert back all captured blocks
						for (net.minecraftforge.common.util.BlockSnapshot blocksnapshot : blockSnapshots){
							world.restoringBlockSnapshots = true;
							blocksnapshot.restore(true, false);
							world.restoringBlockSnapshots = false;
						}
						return false;
					} else {
						// Change the stack to its new content
						if (nbt != null){
							stack.setTagCompound(newNBT);
						}

						for (net.minecraftforge.common.util.BlockSnapshot blocksnapshot : blockSnapshots){
							int blockX = blocksnapshot.x;
							int blockY = blocksnapshot.y;
							int blockZ = blocksnapshot.z;
							int metadata = world.getBlockMetadata(blockX, blockY, blockZ);
							int updateFlag = blocksnapshot.flag;
							Block oldBlock = blocksnapshot.replacedBlock;
							Block newBlock = world.getBlock(blockX, blockY, blockZ);
							if (newBlock != null && !(newBlock.hasTileEntity(metadata))){ // Containers get placed automatically
								newBlock.onBlockAdded(world, blockX, blockY, blockZ);
							}

							world.markAndNotifyBlock(blockX, blockY, blockZ, null, oldBlock, newBlock, updateFlag);
						}
					}
					world.capturedBlockSnapshots.clear();

					// restore((EntityPlayer)caster, world, originalSpellStack, blockx, blocky, blockz, impactX, impactY, impactZ);
				}
			}else{

				if (block == null || block.getBlockHardness(world, blockx, blocky, blockz) == -1.0f){
					return false;
				}

				NBTTagCompound data = new NBTTagCompound();
				data.setString(storageType, "block");
				//data.setString("blockName", block.getUnlocalizedName().replace("tile.", ""));
				data.setInteger("blockID", Block.getIdFromBlock(block));
				int meta = world.getBlockMetadata(blockx, blocky, blockz);
				data.setInteger("meta", meta);

				EntityPlayerMP casterPlayer = (EntityPlayerMP)DummyEntityPlayer.fromEntityLiving(caster);
				if (!ForgeEventFactory.doPlayerHarvestCheck(casterPlayer, block, true)){
					return false;
				}

				BreakEvent event = ForgeHooks.onBlockBreakEvent(world, casterPlayer.theItemInWorldManager.getGameType(), casterPlayer, blockx, blocky, blockz);
				if (event.isCanceled()){
					return false;
				}

				TileEntity te = world.getTileEntity(blockx, blocky, blockz);
				if (te != null){
					NBTTagCompound teData = new NBTTagCompound();
					te.writeToNBT(teData);
					data.setTag("tileEntity", teData);

					// remove tile entity first to prevent content dropping which is already saved in the NBT
					try{
						world.removeTileEntity(blockx, blocky, blockz);
					}catch (Throwable exception){
						exception.printStackTrace();
					}
				}

				originalSpellStack.stackTagCompound.setTag(storageKey, data);

				setOriginalSpellStackData((EntityPlayer)caster, originalSpellStack);

				world.setBlockToAir(blockx, blocky, blockz);
			}
		}

		return true;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityPlayer || target instanceof IBossDisplayData)
			return false;

		if (!(target instanceof EntityLivingBase))
			return false;

		for (Class clazz : AMCore.config.getAppropriationMobBlacklist())
			if (target.getClass() == clazz)
				return false;

		if (!(caster instanceof EntityPlayer))
			return false;

		ItemStack originalSpellStack = getOriginalSpellStack((EntityPlayer)caster);
		if (originalSpellStack == null)
			return false;
		if (!world.isRemote){
			if (originalSpellStack.hasTagCompound() && originalSpellStack.stackTagCompound.hasKey(storageKey)){
				restore((EntityPlayer)caster, world, originalSpellStack, (int)target.posX, (int)target.posY, (int)target.posZ, target.posX, target.posY + target.getEyeHeight(), target.posZ);
			}else{
				NBTTagCompound data = new NBTTagCompound();
				data.setString("class", target.getClass().getName());
				data.setString(storageType, "ent");
				NBTTagCompound targetData = new NBTTagCompound();
				target.writeToNBT(targetData);
				data.setTag("targetNBT", targetData);
				originalSpellStack.stackTagCompound.setTag(storageKey, data);
				setOriginalSpellStackData((EntityPlayer)caster, originalSpellStack);
				target.setDead();
			}
		}

		return true;
	}

	private void setOriginalSpellStackData(EntityPlayer caster, ItemStack modifiedStack){
		ItemStack originalSpellStack = caster.getCurrentEquippedItem();
		if (originalSpellStack == null)
			return;
		if (originalSpellStack.getItem() instanceof ItemSpellBook){
			((ItemSpellBook)originalSpellStack.getItem()).replaceAciveItemStack(originalSpellStack, modifiedStack);
		}else{
			caster.inventory.setInventorySlotContents(caster.inventory.currentItem, modifiedStack);
		}
	}

	private ItemStack getOriginalSpellStack(EntityPlayer caster){
		ItemStack originalSpellStack = caster.getCurrentEquippedItem();
		if (originalSpellStack == null ){return null;}
		if(originalSpellStack.getItem() == ItemsCommonProxy.spell){return originalSpellStack;}
		else if (originalSpellStack.getItem() instanceof ItemSpellBook){
			originalSpellStack = ((ItemSpellBook)originalSpellStack.getItem()).GetActiveItemStack(originalSpellStack); //it's a spell book - get the active scroll
			//sanity check needed here because from cast to apply the spell could have changed - just ensure appropriation is a part of this spell somewhere so that any stored item can be retrieved
			for (int i = 0; i < SpellUtils.instance.numStages(originalSpellStack); ++i){
				if (SpellUtils.instance.componentIsPresent(originalSpellStack, Appropriation.class, i)){
					return originalSpellStack;
				}
			}
		}
		return null;
	}

	private void restore(EntityPlayer player, World world, ItemStack stack, int x, int y, int z, double hitX, double hitY, double hitZ){
		if (stack.stackTagCompound.hasKey(storageKey)){
			NBTTagCompound storageCompound = stack.stackTagCompound.getCompoundTag(storageKey);
			if (storageCompound != null){
				String type = storageCompound.getString(storageType);
				if (type.equals("ent")){
					String clazz = storageCompound.getString("class");
					NBTTagCompound entData = storageCompound.getCompoundTag("targetNBT");
					try{
						Entity ent = (Entity)Class.forName(clazz).getConstructor(World.class).newInstance(world);
						ent.readFromNBT(entData);
						ent.setPosition(hitX, hitY, hitZ);
						world.spawnEntityInWorld(ent);
					}catch (Throwable t){
						t.printStackTrace();
					}
				}else if (type.equals("block")){
					//String blockName = storageCompound.getString("blockName");
					int blockID = storageCompound.getInteger("blockID");
					int meta = storageCompound.getInteger("meta");

					//Block block = Block.getBlockFromName(blockName);
					Block block = Block.getBlockById(blockID);
					if (block != null){
						world.setBlock(x, y, z, block, meta, 2);
					}else{
						if (!player.worldObj.isRemote)
							player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.approError")));
						stack.stackTagCompound.removeTag(storageKey);
						return;
					}


					if (storageCompound.hasKey("tileEntity")){
						TileEntity te = world.getTileEntity(x, y, z);
						if (te != null){
							te.readFromNBT(storageCompound.getCompoundTag("tileEntity"));
							te.xCoord = x;
							te.yCoord = y;
							te.zCoord = z;
							te.setWorldObj(world);
						}
					}
				}
			}

			stack.stackTagCompound.removeTag(storageKey);

			setOriginalSpellStackData(player, stack);
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 415;
	}

	@Override
	public float burnout(EntityLivingBase caster){
		return 100;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 5 + 5 * AMCore.config.getGFXLevel(); ++i){
			AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "water_ball", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.setMaxAge(10);
				particle.setParticleScale(0.1f);
				particle.AddParticleController(new ParticleOrbitPoint(particle, x, y, z, 1, false).SetTargetDistance(world.rand.nextDouble() + 0.1f).SetOrbitSpeed(0.2f));
			}
		}
	}

	@Override
	public EnumSet<Affinity> getAffinity(){
		return EnumSet.of(Affinity.WATER);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0;
	}
}
