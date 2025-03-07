package am2.blocks.tileentities;

import am2.AMCore;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.blocks.MultiblockStructureDefinition.StructureGroup;
import am2.api.power.IPowerSource;
import am2.api.power.PowerTypes;
import am2.blocks.BlockAMOre;
import am2.blocks.BlocksCommonProxy;
import am2.buffs.BuffEffectManaRegen;
import am2.buffs.BuffList;
import am2.multiblock.IMultiblockStructureController;
import am2.power.PowerNodeRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityCelestialPrism extends TileEntityPowerSources implements IMultiblockStructureController, IPowerSource{

	private StructureGroup moonstone;
	private int ticks;
	private boolean isOnlyChargeAtNight = false;

	public TileEntityCelestialPrism(){
		super(5000, PowerTypes.LIGHT);
		GenerateStructureData();

	}


	protected void checkNearbyBlockState(){
		ArrayList<StructureGroup> groups = structure.getMatchedGroups(7, worldObj, xCoord, yCoord, zCoord);

		int capsLevel = 1;
		boolean pillarsFound = false;

		this.powerMultiplier = 1;
		for (StructureGroup group : groups){
			if (group == pillars)
				this.powerMultiplier +=1;
			else if (group == wizardChalkCircle)
				this.powerMultiplier +=1;


			for (StructureGroup cap : caps.keySet()){
				if (group == cap){
					capsLevel = caps.get(cap);
					if (group == moonstone){
						isOnlyChargeAtNight = true;
						if (!worldObj.isRemote &&  isNight()){
							powerMultiplier *= 4;
						}
					}
					break;
				}
				this.powerMultiplier *= capsLevel;
				break;
			}
		}
	}

	private boolean isNight(){
		long ticks = worldObj.getWorldTime() % 24000;
		return ticks >= 12500 && ticks <= 23500;
	}

	@Override
	public void updateEntity(){

		ticks++;
		if(ticks > 360){
			ticks = 1;
		}
		if (ticks % 100 == 0){
			checkNearbyBlockState();
			if (!worldObj.isRemote && this.getCharge() >= this.getCapacity() * 0.1f){
				List<EntityPlayer> nearbyPlayers = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.xCoord - 2, this.yCoord, this.zCoord - 2, this.xCoord + 2, this.yCoord + 3, this.zCoord + 2));
				for (EntityPlayer p : nearbyPlayers){
					if (p.isPotionActive(BuffList.manaRegen.id)) continue;
					p.addPotionEffect(new BuffEffectManaRegen(600, 1));
				}
			}

		}
		if(!worldObj.isRemote){
		if(ticks % 20 == 0){
			if (isOnlyChargeAtNight){
				if (isNight()){
					setCharge((powerBase * powerMultiplier));
				}
			}else if(!isNight()){
				setCharge((powerBase * powerMultiplier));
			}
			markDirty();
		}
		}
			if (worldObj.isRemote){

				if (ticks % 180 == 0){
					AMCore.proxy.particleManager.RibbonFromPointToPoint(worldObj, xCoord + worldObj.rand.nextFloat(), yCoord + (worldObj.rand.nextFloat() * 2), zCoord + worldObj.rand.nextFloat(), xCoord + worldObj.rand.nextFloat(), yCoord + (worldObj.rand.nextFloat() * 2), zCoord + worldObj.rand.nextFloat());
				}
			}
	}
	public void GenerateStructureData(){
		structure = new MultiblockStructureDefinition("celestialprism_structure");

		StructureGroup glass = structure.createGroup("caps_glass", 2);
		StructureGroup gold = structure.createGroup("caps_gold", 2);
		StructureGroup diamond = structure.createGroup("caps_diamond", 2);
		moonstone = structure.createGroup("caps_moonstone", 2);

		pillars = structure.createGroup("pillars", 4);

		caps = new HashMap<StructureGroup, Integer>();
		caps.put(glass, 1);
		caps.put(gold, 2);
		caps.put(diamond, 2);
		caps.put(moonstone, 1);

		structure.addAllowedBlock(0, 0, 0, BlocksCommonProxy.celestialPrism);

		structure.addAllowedBlock(pillars, -2, 0, -2, Blocks.quartz_block);
		structure.addAllowedBlock(pillars, -2, 1, -2, Blocks.quartz_block);

		structure.addAllowedBlock(glass, -2, 2, -2, Blocks.glass);
		structure.addAllowedBlock(gold, -2, 2, -2, Blocks.gold_block);
		structure.addAllowedBlock(diamond, -2, 2, -2, Blocks.diamond_block);
		structure.addAllowedBlock(moonstone, -2, 2, -2, BlocksCommonProxy.AMOres, BlockAMOre.META_MOONSTONE_BLOCK);

		structure.addAllowedBlock(pillars, 2, 0, -2, Blocks.quartz_block);
		structure.addAllowedBlock(pillars, 2, 1, -2, Blocks.quartz_block);

		structure.addAllowedBlock(glass, 2, 2, -2, Blocks.glass);
		structure.addAllowedBlock(gold, 2, 2, -2, Blocks.gold_block);
		structure.addAllowedBlock(diamond, 2, 2, -2, Blocks.diamond_block);
		structure.addAllowedBlock(moonstone, 2, 2, -2, BlocksCommonProxy.AMOres, BlockAMOre.META_MOONSTONE_BLOCK);

		structure.addAllowedBlock(pillars, -2, 0, 2, Blocks.quartz_block);
		structure.addAllowedBlock(pillars, -2, 1, 2, Blocks.quartz_block);

		structure.addAllowedBlock(glass, -2, 2, 2, Blocks.glass);
		structure.addAllowedBlock(gold, -2, 2, 2, Blocks.gold_block);
		structure.addAllowedBlock(diamond, -2, 2, 2, Blocks.diamond_block);
		structure.addAllowedBlock(moonstone, -2, 2, 2, BlocksCommonProxy.AMOres, BlockAMOre.META_MOONSTONE_BLOCK);

		structure.addAllowedBlock(pillars, 2, 0, 2, Blocks.quartz_block);
		structure.addAllowedBlock(pillars, 2, 1, 2, Blocks.quartz_block);

		structure.addAllowedBlock(glass, 2, 2, 2, Blocks.glass);
		structure.addAllowedBlock(gold, 2, 2, 2, Blocks.gold_block);
		structure.addAllowedBlock(diamond, 2, 2, 2, Blocks.diamond_block);
		structure.addAllowedBlock(moonstone, 2, 2, 2, BlocksCommonProxy.AMOres, BlockAMOre.META_MOONSTONE_BLOCK);

		wizardChalkCircle = structure.addWizChalkGroupToStructure(1);
	}
}
