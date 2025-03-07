package am2.blocks;

import am2.AMCreativeTab;
import am2.api.blocks.IKeystoneLockable;
import am2.api.math.AMVector3;
import am2.api.spell.enums.Affinity;
import am2.blocks.liquid.BlockLiquidEssence;
import am2.blocks.tileentities.*;
import am2.items.ItemEssence;
import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import am2.items.OreItem;
import am2.spell.SkillManager;
import am2.spell.components.Dig;
import am2.utility.KeystoneUtilities;
import am2.utility.RecipeUtilities;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlocksCommonProxy{
	//--------------------------------------------------------------
	// Blocks
	//--------------------------------------------------------------
	public static Block essenceRefiner;
	public static IllusionBlock illusionBlock;
	public static Block blockMageTorch;
	public static Block essenceConduit;
	public static Block obelisk;
	public static Block blackAurem;
	public static Block spatialVortex;
	public static Block celestialPrism;
	public static Block caster;
	public static Block calefactor;
	public static BlockKeystoneReceptacle keystoneRecepticle;
	public static Block astralBarrier;
	public static Block seerStone;
	public static BlockAMOre AMOres;
	public static AMFlower cerublossom;
	public static BlockDesertNova desertNova;
	public static BlockKeystoneChest keystoneChest;
	public static Block blockLectern;
	public static Block blockArcaneReconstructor;
	public static Block manaBattery;
	public static Block magicWall;
	public static Block occulus;
	public static BlockCraftingAltar craftingAltar;
	public static BlockGroundRuneSpell spellRune;
	public static BlockParticleEmitter particleEmitter;
	public static VinteumTorch vinteumTorch;
	public static BlockCasterRune casterRune;
	public static BlockInscriptionTable inscriptionTable;
	public static BlockInvisibleUtility invisibleUtility;
	public static AMFlower aum;
	public static BlockWakebloom wakebloom;
	public static BlockTarmaRoot tarmaRoot;
	public static BlockInfusedStem infusedStem;
	public static BlockSanguineAmaryllis sanguineAmaryllis;
	public static BlockEffervescentSnowdrop effervescentSnowdrop;
	public static BlockWitchwoodLog witchwoodLog;
	public static BlockWitchwoodLogDrained witchwoodLogDrained;
	public static BlockWitchwoodLeaves witchwoodLeaves;
	public static BlockSummoner summoner;
	public static BlockLiquidEssence liquidEssence;
	public static BlockInlay redstoneInlay;
	public static BlockInlay ironInlay;
	public static BlockInlay goldInlay;
	public static WitchwoodPlanks witchwoodPlanks;
	public static WitchwoodSlabs witchwoodSingleSlab;
	public static WitchwoodSlabs witchwoodDoubleSlab;
	public static WitchwoodStairs witchwoodStairs;
	public static WitchwoodSapling witchwoodSapling;
	public static BlockMagiciansWorkbench magiciansWorkbench;
	public static BlockEverstone everstone;
	public static BlockKeystoneDoor keystoneDoor;
	public static BlockKeystoneTrapdoor keystoneTrapDoor;
	public static BlockSlipstreamGenerator slipstreamGenerator;
	public static BlockCrystalMarker crystalMarker;
	public static BlockFlickerHabitat elementalAttuner;
	public static BlockWizardsChalk wizardChalk;
	public static BlockInertSpawner inertSpawner;
	public static BlockCandle candle;
	public static BlockBrokenPowerLink brokenLinkBlock;
	public static BlockArmorInfuser armorInfuser;
	public static BlockFlickerLure flickerLure;
	public static BlockArcaneDeconstructor arcaneDeconstructor;
	public static BlockOtherworldAura otherworldAura;
	public static BlockSpellSealedDoor spellSealedDoor;
	public static BlockEntropicEnervator entropicEnervator;
	public static BlockSpellReplicator spellReplicator;
	public static BlockRainRockrose rainRockrose;
	public static BlockStormSawtooth stormSawtooth;
	public static BlockImbuedMoonflower imbuedMoonflower;

	//--------------------------------------------------------------
	// End Blocks
	//---------------------------------------------------------------

	public static int blockRenderID;
	public static int commonBlockRenderID;
	public static AMCreativeTab blockTab;

	private static ArrayList<Block> arsMagicaBlocksList;

	public static HashMap<Integer, ArrayList<AMVector3>> KeystonePortalLocations;

	public BlocksCommonProxy(){
		KeystonePortalLocations = new HashMap<Integer, ArrayList<AMVector3>>();
		if (blockTab == null)
			blockTab = new AMCreativeTab("Ars Magica Blocks");

		if (arsMagicaBlocksList == null)
			arsMagicaBlocksList = new ArrayList<Block>();
	}

	public List<Block> getArsMagicaBlocks(){
		return arsMagicaBlocksList;
	}

	public void registerKeystonePortal(int x, int y, int z, int dimension){
		AMVector3 location = new AMVector3(x, y, z);
		if (!KeystonePortalLocations.containsKey(dimension))
			KeystonePortalLocations.put(dimension, new ArrayList<AMVector3>());

		ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(dimension);

		if (!dimensionList.contains(location))
			dimensionList.add(location);
	}

	public void removeKeystonePortal(int x, int y, int z, int dimension){
		AMVector3 location = new AMVector3(x, y, z);
		if (KeystonePortalLocations.containsKey(dimension)){
			ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(dimension);

			dimensionList.remove(location);
		}
	}

	public AMVector3 getNextKeystonePortalLocation(World world, int x, int y, int z, boolean multidimensional, long key){
		AMVector3 current = new AMVector3(x, y, z);
		if (!multidimensional){
			AMVector3 next = getNextKeystoneLocationInWorld(world, x, y, z, key);
			if (next == null)
				next = current;
			return next;
		}else{
			return current;
		}
	}

	public AMVector3 getNextKeystoneLocationInWorld(World world, int x, int y, int z, long key){
		AMVector3 location = new AMVector3(x, y, z);
		ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(world.provider.dimensionId);
		if (dimensionList == null || dimensionList.isEmpty()){
			return null;
		}

		int index = dimensionList.indexOf(location);
		index++;
		if (index >= dimensionList.size()) index = 0;
		AMVector3 newLocation = dimensionList.get(index);

		while (!newLocation.equals(location)){
			TileEntity te = world.getTileEntity((int)newLocation.x, (int)newLocation.y, (int)newLocation.z);
			if (te instanceof TileEntityKeystoneRecepticle){
				if (KeystoneUtilities.instance.getKeyFromRunes(((IKeystoneLockable)te).getRunesInKey()) == key){
					return newLocation;
				}
			}
			index++;
			if (index >= dimensionList.size()) index = 0;
			newLocation = dimensionList.get(index);
		}

		return location;
	}

	public void resetKnownPortalLocations(){
		KeystonePortalLocations.clear();
	}

	public void InstantiateBlocks(){
		essenceRefiner = new BlockEssenceRefiner().setUnlocalizedNameAndID("arsmagica2:essence_refiner").setCreativeTab(blockTab);
		blockMageTorch = new BlockMageLight().setUnlocalizedNameAndID("arsmagica2:magetorch").setCreativeTab(blockTab);
		illusionBlock = (IllusionBlock)new IllusionBlock().setUnlocalizedNameAndID("arsmagica2:illusionBlock").setCreativeTab(blockTab);
		essenceConduit = new BlockEssenceConduit().setUnlocalizedNameAndID("arsmagica2:essenceconduit").setCreativeTab(blockTab);
		caster = (new BlockCaster()).setUnlocalizedNameAndID("arsmagica2:blockcaster").setCreativeTab(blockTab);
		casterRune = (BlockCasterRune)(new BlockCasterRune()).setUnlocalizedNameAndID("arsmagica2:casterRune").setCreativeTab(blockTab);

		obelisk = new BlockEssenceGenerator(BlockEssenceGenerator.NEXUS_STANDARD).setUnlocalizedNameAndID("arsmagica2:obelisk").setCreativeTab(blockTab);
		calefactor = new BlockCalefactor().setUnlocalizedNameAndID("arsmagica2:calefactor").setCreativeTab(blockTab);
		keystoneRecepticle = (BlockKeystoneReceptacle)new BlockKeystoneReceptacle().setUnlocalizedNameAndID("arsmagica2:blockkeystonerecepticle").setCreativeTab(blockTab);
		astralBarrier = new BlockAstralBarrier().setUnlocalizedNameAndID("arsmagica2:blockastralbarrier").setCreativeTab(blockTab);
		blackAurem = new BlockEssenceGenerator(BlockEssenceGenerator.NEXUS_DARK).setUnlocalizedNameAndID("arsmagica2:blackaurem").setCreativeTab(blockTab);
		spatialVortex = new BlockSpatialVortex().setUnlocalizedNameAndID("arsmagica2:spatialVortex").setCreativeTab(blockTab);
		seerStone = new BlockSeerStone().setUnlocalizedNameAndID("arsmagica2:blockseerstone").setCreativeTab(blockTab);
		celestialPrism = new BlockEssenceGenerator(BlockEssenceGenerator.NEXUS_LIGHT).setUnlocalizedNameAndID("arsmagica2:celestialprism").setCreativeTab(blockTab);
		AMOres = (BlockAMOre)new BlockAMOre().setBlockName("arsmagica2:ores").setHardness(3.0f).setResistance(3.0f).setCreativeTab(blockTab);
		cerublossom = (AMFlower)new AMFlower().setUnlocalizedNameAndID("arsmagica2:cerublossom").setLightLevel(0.325f).setCreativeTab(blockTab);
		desertNova = (BlockDesertNova)new BlockDesertNova().setUnlocalizedNameAndID("arsmagica2:desert_nova").setCreativeTab(blockTab);
		keystoneChest = (BlockKeystoneChest)new BlockKeystoneChest().setUnlocalizedNameAndID("arsmagica2:keystonechest").setCreativeTab(blockTab);
		blockLectern = new BlockLectern().setUnlocalizedNameAndID("arsmagica2:lectern").setCreativeTab(blockTab);
		blockArcaneReconstructor = new BlockArcaneReconstructor().setUnlocalizedNameAndID("arsmagica2:arcanereconstructor").setCreativeTab(blockTab);
		manaBattery = new BlockManaBattery().setUnlocalizedNameAndID("arsmagica2:mana_battery").setCreativeTab(blockTab);
		magicWall = new BlockMagicWall().setUnlocalizedNameAndID("arsmagica2:magicWall").setCreativeTab(blockTab);
		occulus = new BlockOcculus().setUnlocalizedNameAndID("arsmagica2:occulus").setCreativeTab(blockTab);
		craftingAltar = (BlockCraftingAltar)new BlockCraftingAltar().setUnlocalizedNameAndID("arsmagica2:altarOfCreation").setCreativeTab(blockTab);
		spellRune = (BlockGroundRuneSpell)new BlockGroundRuneSpell().setUnlocalizedNameAndID("arsmagica2:spellRune");
		particleEmitter = (BlockParticleEmitter)new BlockParticleEmitter().setUnlocalizedNameAndID("arsmagica2:particleEmitter").setCreativeTab(blockTab);
		vinteumTorch = (VinteumTorch)new VinteumTorch().setBlockName("arsmagica2:vinteumTorch").setCreativeTab(blockTab);
		inscriptionTable = (BlockInscriptionTable)new BlockInscriptionTable().setUnlocalizedNameAndID("arsmagica2:inscriptionTable").setCreativeTab(blockTab);
		invisibleUtility = (BlockInvisibleUtility)new BlockInvisibleUtility().setUnlocalizedNameAndID("arsmagica2:invisibleUtility");
		aum = (AMFlower)new AMFlower().setUnlocalizedNameAndID("arsmagica2:aum").setCreativeTab(blockTab);
		wakebloom = (BlockWakebloom)new BlockWakebloom().setUnlocalizedNameAndID("arsmagica2:wakebloom").setCreativeTab(blockTab);
		tarmaRoot = (BlockTarmaRoot)new BlockTarmaRoot().setUnlocalizedNameAndID("arsmagica2:tarmaroot").setCreativeTab(blockTab);
		effervescentSnowdrop = (BlockEffervescentSnowdrop) new BlockEffervescentSnowdrop().setUnlocalizedNameAndID("arsmagica2:effervescentSnowdrop").setCreativeTab(blockTab);
		sanguineAmaryllis = (BlockSanguineAmaryllis) new BlockSanguineAmaryllis().setUnlocalizedNameAndID("arsmagica2:sanguineAmaryllis").setCreativeTab(blockTab);
		infusedStem = (BlockInfusedStem) new BlockInfusedStem().setUnlocalizedNameAndID("arsmagica2:infusedStem").setCreativeTab(blockTab);
		witchwoodLog = (BlockWitchwoodLog)new BlockWitchwoodLog().setBlockName("arsmagica2:witchwoodlog").setCreativeTab(blockTab);
		witchwoodLogDrained = (BlockWitchwoodLogDrained) new BlockWitchwoodLogDrained().setBlockName("arsmagica2:drainedwitchwoodlog").setCreativeTab(blockTab);
		witchwoodLeaves = (BlockWitchwoodLeaves)new BlockWitchwoodLeaves().setBlockName("arsmagica2:witchwoodleaves").setCreativeTab(blockTab);
		summoner = (BlockSummoner)new BlockSummoner().setUnlocalizedNameAndID("arsmagica2:summoner").setCreativeTab(blockTab);
		liquidEssence = (BlockLiquidEssence)new BlockLiquidEssence().setBlockName("arsmagica2:liquidEssence");
		redstoneInlay = (BlockInlay)new BlockInlay(BlockInlay.TYPE_REDSTONE).setBlockName("arsmagica2:redstone_inlay").setCreativeTab(blockTab);
		ironInlay = (BlockInlay)new BlockInlay(BlockInlay.TYPE_IRON).setBlockName("arsmagica2:iron_inlay").setCreativeTab(blockTab);
		goldInlay = (BlockInlay)new BlockInlay(BlockInlay.TYPE_GOLD).setBlockName("arsmagica2:gold_inlay").setCreativeTab(blockTab);
		witchwoodPlanks = (WitchwoodPlanks)new WitchwoodPlanks().setBlockName("arsmagica2:planksWitchwood").setCreativeTab(blockTab);
		witchwoodSingleSlab = (WitchwoodSlabs)new WitchwoodSlabs(false).setBlockName("arsmagica2:witchwoodSingleSlab").setCreativeTab(blockTab);
		witchwoodDoubleSlab = (WitchwoodSlabs)new WitchwoodSlabs(true).setBlockName("arsmagica2:witchwoodDoubleSlab");
		witchwoodStairs = (WitchwoodStairs)new WitchwoodStairs(witchwoodPlanks, 0).setBlockName("arsmagica2:stairsWitchwood").setCreativeTab(blockTab);
		witchwoodSapling = (WitchwoodSapling)new WitchwoodSapling().setBlockName("arsmagica2:saplingWitchwood").setCreativeTab(blockTab);
		magiciansWorkbench = (BlockMagiciansWorkbench)new BlockMagiciansWorkbench().setBlockName("arsmagica2:magiciansWorkbench").setCreativeTab(blockTab);
		everstone = (BlockEverstone)new BlockEverstone().setBlockName("arsmagica2:everstone").setCreativeTab(blockTab);
		keystoneDoor = (BlockKeystoneDoor)new BlockKeystoneDoor().setBlockName("arsmagica2:keystoneDoor");
		keystoneTrapDoor = (BlockKeystoneTrapdoor)new BlockKeystoneTrapdoor().setBlockName("arsmagica2:keystoneTrapdoor");
		slipstreamGenerator = (BlockSlipstreamGenerator)new BlockSlipstreamGenerator().setBlockName("arsmagica2:slipstreamGenerator").setCreativeTab(blockTab);
		crystalMarker = (BlockCrystalMarker)new BlockCrystalMarker().setBlockName("arsmagica2:crystalMarker").setCreativeTab(blockTab);
		elementalAttuner = (BlockFlickerHabitat)new BlockFlickerHabitat().setBlockName("arsmagica2:flickerHabitat").setCreativeTab(blockTab);
		wizardChalk = (BlockWizardsChalk)new BlockWizardsChalk().setBlockName("arsmagica2:wizardChalk");
		inertSpawner = (BlockInertSpawner)new BlockInertSpawner().setBlockName("arsmagica2:inertSpawner").setCreativeTab(blockTab);
		candle = (BlockCandle)new BlockCandle().setBlockName("arsmagica2:candle").setCreativeTab(blockTab);
		brokenLinkBlock = (BlockBrokenPowerLink)new BlockBrokenPowerLink().setBlockName("arsmagica2:brokenLink").setCreativeTab(blockTab);
		armorInfuser = (BlockArmorInfuser)new BlockArmorInfuser().setBlockName("arsmagica2:armor_infuser").setCreativeTab(blockTab);
		flickerLure = (BlockFlickerLure)new BlockFlickerLure().setBlockName("arsmagica2:flicker_lure").setCreativeTab(blockTab);
		arcaneDeconstructor = (BlockArcaneDeconstructor)new BlockArcaneDeconstructor().setBlockName("arsmagica2:arcane_deconstructor").setCreativeTab(blockTab);
		otherworldAura = (BlockOtherworldAura)new BlockOtherworldAura().setBlockName("arsmagica2:otherworld_aura").setCreativeTab(blockTab);
		spellSealedDoor = (BlockSpellSealedDoor)new BlockSpellSealedDoor().setBlockName("arsmagica2:spell_sealed_door");
		entropicEnervator = (BlockEntropicEnervator)new BlockEntropicEnervator().setBlockName("arsmagica2:entropic_enervator").setCreativeTab(blockTab);
		spellReplicator = (BlockSpellReplicator)new BlockSpellReplicator().setBlockName("arsmagica2:spellReplicator").setCreativeTab(blockTab);

		rainRockrose = (BlockRainRockrose) new BlockRainRockrose().setUnlocalizedNameAndID("arsmagica2:rainRockrose");
		stormSawtooth = (BlockStormSawtooth) new BlockStormSawtooth().setUnlocalizedNameAndID("arsmagica2:stormSawtooth");
		imbuedMoonflower = (BlockImbuedMoonflower) new BlockImbuedMoonflower().setUnlocalizedNameAndID("arsmagica2:imbuedMoonflower");

		blockTab.setIconItemIndex(new ItemBlock(manaBattery));
	}

	public void setupSpellConstraints(){
		Dig dug = ((Dig)SkillManager.instance.getSkill("Dig"));
		dug.addDisallowedBlock(invisibleUtility.getUnlocalizedName());
	}

	public void InitRecipes(){

		List recipes = CraftingManager.getInstance().getRecipeList();

		//essence refiner
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essenceRefiner, 1),
				new Object[]
						{
								"PDP", "OAO", "PPP",
								'P', "plankWood",
								'O', Blocks.obsidian,
								'A', "arcaneAsh",
								'D', "gemDiamond"
						}));

		//essence conduit
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essenceConduit, 1), new Object[]{
				" C ", " S ", "SSS",
				'S', "stone",
				'C', "gemChimerite"
		}));

		//caster
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(caster, 1), new Object[]{
				"SRS", "AVA", "SFS",
				'A', "arcaneAsh",
				'S', "stone",
				'F', new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_SOULFRAGMENT),
				'R', "dustRedstone",
				'V', "dustVinteum"}));

		//caster rune
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(casterRune, 1), new Object[]{
				"RRR", "APA", "SVS",
				'A', "arcaneAsh",
				'S', "stone",
				'V', "dustVinteum",
				'R', ItemsCommonProxy.rune,
				'P', new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_SOULFRAGMENT)
		}));

		//summoner
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(summoner, 1), new Object[]{
				"GVG", "GOG", "OOO",
				'G', "ingotGold",
				'O', Blocks.obsidian,
				'V', "dustVinteum"
		}));

		//Calefactor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(calefactor, 1), new Object[]{
				"L L",
				"SRS",
				"SVS",
				'L', new ItemStack(Items.dye, 1, 4), //lapis
				'S', "stone",
				'R', "dustRedstone",
				'V', "dustVinteum"
		}));

		//keystone recepticle
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(keystoneRecepticle, 1), new Object[]{
				"SVS", "EPE", "SVS",
				'P', new ItemStack(ItemsCommonProxy.essence, 1, 9),
				'S', Blocks.stonebrick,
				'E', Items.ender_eye,
				'V', "dustVinteum"
		}));

		//astral barrier
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(astralBarrier, 1), new Object[]{
				"WVW", "E E", "WVW",
				//Character.valueOf('P'), new ItemStack(ItemsCommonProxy.essence, 1, ItemsCommonProxy.essence.META_ENDER),
				'W', Blocks.cobblestone_wall,
				'E', Items.ender_eye,
				'V', "dustVinteum"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(seerStone, 1), new Object[]{
				" E ", "SRS",
				'S', "stone", //stone wall
				'E', Items.ender_eye,
				'R', "dustRedstone"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(keystoneChest), new Object[]{
				"WRW", "WVW", "WRW",
				'W', "plankWood",
				'R', new ItemStack(ItemsCommonProxy.rune, 1, 0),
				'V', "dustVinteum"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLectern), new Object[]{
				"SSS", " P ",
				'S', "slabWood",
				'P', "plankWood"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(occulus), new Object[]{
				"SPS", " S ", "CVC",
				'S', Blocks.stonebrick,
				'C', Items.coal,
				'P', "blockGlassColorless",
				'V', "gemBlueTopaz"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockArcaneReconstructor), new Object[]{
				"SWS", "VDV", "SOS",
				'S', "stone",
				'V', "dustVinteum",
				'D', "gemDiamond",
				'W', BlocksCommonProxy.magicWall,
				'O', Blocks.obsidian
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(arcaneDeconstructor), new Object[]{
				"IGR",
				"WDW",
				"CWC",
				'I', ItemsCommonProxy.itemFocus,
				'G', "blockGlassColorless",
				'R', new ItemStack(ItemsCommonProxy.essence, 1, ItemEssence.META_PURE),
				'W', witchwoodPlanks,
				'D', ItemsCommonProxy.deficitCrystal,
				'C', new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_PYROGENICSEDIMENT),
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flickerLure), new Object[]{
				"CIV",
				"SSS",
				'C', "gemChimerite",
				'I', "ingotIron",
				'V', "dustVinteum",
				'S', Blocks.stonebrick
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(manaBattery), new Object[]{
				"IVI",
				"VAV",
				"IVI",
				'I', "gemChimerite",
				'V', "dustVinteum",
				'A', "arcaneAsh"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(magicWall, 16, 0), new Object[]{
				"VSV",
				'V', "dustVinteum",
				'S', "stone"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(craftingAltar), new Object[]{
				"V",
				"S",
				'V', "dustVinteum",
				'S', "stone"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(vinteumTorch, 4), new Object[]{
				"V",
				"S",
				'V', "dustVinteum",
				'S', "stickWood"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(inscriptionTable, new Object[]{
				"TPF",
				"SSS",
				"W W",
				'T', Blocks.torch,
				'P', ItemsCommonProxy.spellParchment,
				'F', Items.feather,
				'S', "slabWood",
				'W', "plankWood"
		}));

		GameRegistry.addRecipe(new ItemStack(Blocks.stonebrick, 1, 3), new Object[]{
				"SS",
				"SS",
				'S', Blocks.stonebrick
		});

		//Inlays

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(redstoneInlay, 4, 0), new Object[]{
				"RRR",
				"RVR",
				"RRR",
				'R', "dustRedstone",
				'V', "dustVinteum"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ironInlay, 4, 0), new Object[]{
				"III",
				"IVI",
				"III",
				'I', "ingotIron",
				'V', "arcaneAsh"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(goldInlay, 4, 0), new Object[]{
				"GGG",
				"GVG",
				"GGG",
				'G', "ingotGold",
				'V', new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_PURIFIEDVINTEUM)
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(particleEmitter), new Object[]{
				" C ",
				"CIC",
				" C ",
				'I', illusionBlock,
				'C', "gemChimerite"
		}));

		GameRegistry.addRecipe(new ItemStack(witchwoodPlanks, 4), new Object[]{
				"W",
				'W', witchwoodLog
		});

		RecipeUtilities.addShapedRecipeFirst(recipes, new ItemStack(witchwoodSingleSlab, 6), new Object[]{
				"WWW",
				'W', witchwoodPlanks
		});

		RecipeUtilities.addShapedRecipeFirst(recipes, new ItemStack(witchwoodStairs, 4), new Object[]{
				"  W",
				" WW",
				"WWW",
				'W', witchwoodPlanks
		});

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(everstone), new Object[]{
				" B ",
				"CSC",
				" B ",
				'C', "gemChimerite",
				'S', "stone",
				'B', "gemBlueTopaz"
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(magiciansWorkbench), new Object[]{
				"COC",
				"SWS",
				"LHL",
				'C', "craftingTableWood",
				'O', new ItemStack(Blocks.carpet),
				'W', "logWood",
				'S', "slabWood",
				'L', "plankWood",
				'H', "chestWood"
		}));

		GameRegistry.addRecipe(new ItemStack(slipstreamGenerator), new Object[]{
				"WWW",
				"FAF",
				"WWW",
				'W', witchwoodLog,
				'F', Items.feather,
				'A', new ItemStack(ItemsCommonProxy.essence, 1, ItemEssence.META_AIR)
		});

		//Flicker Habitat
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(elementalAttuner), new Object[]{
				"IDI",
				"DBD",
				"IDI",
				'I', "ingotIron",
				'D', "dustVinteum",
				'B', new ItemStack(AMOres, 1, BlockAMOre.META_CHIMERITE_BLOCK)
		}));

		//Import Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_IN), new Object[]{
				" G ",
				"GDG",
				" G ",
				'G', "gemBlueTopaz",
				'D', "dyeYellow" //Yellow Dye
		}));


		//Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_OUT), new Object[]{
				" G ",
				"GDG",
				" G ",
				'G', "gemBlueTopaz",
				'D', "dyeBlue" //Lapis
		}));

		//Final Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_FINAL_DEST), new Object[]{
				" G ",
				"GDG",
				" G ",
				'G', "gemBlueTopaz",
				'D', "dyeGray" //Lapis
		}));

		//Like Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_LIKE_EXPORT), new Object[]{
				"GDG",
				"DED",
				"GDG",
				'G', "gemBlueTopaz",
				'D', "dyeGreen", //Cactus Green
				'E', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_OUT)
		}));

		//Regulate Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_REGULATE_EXPORT), new Object[]{
				"GDG",
				"DED",
				"GDG",
				'G', "gemBlueTopaz",
				'D', "dyePurple", //Purple Dye
				'E', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_OUT)
		}));

		//Set Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SET_EXPORT), new Object[]{
				"GDG",
				"DED",
				"GDG",
				'G', "gemBlueTopaz",
				'D', "dyeLightBlue", //Light Blue Dye
				'E', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_OUT)
		}));

		//Regulate Bidirectional
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_REGULATE_MULTI), new Object[]{
				"DSD",
				"GEG",
				"DSD",
				'S', "gemSunstone",
				'G', "gemBlueTopaz",
				'D', "dyeOrange",
				'E', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_REGULATE_EXPORT)
		}));

		//Set Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SET_IMPORT), new Object[]{
				"DSD",
				"GEG",
				"DSD",
				'S', "gemSunstone",
				'G', "gemBlueTopaz",
				'D', "dyeRed",
				'E', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SET_EXPORT)
		}));

		//Spell Export Gem
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SPELL_EXPORT), new Object[]{
				"C C",
				"RPI",
				"C C",
				'P', new ItemStack(ItemsCommonProxy.essence, 1, ItemEssence.META_PURE),
				'I', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SET_IMPORT),
				'C', "dyeCyan",
				'R', new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_REGULATE_MULTI)
		}));

		//Gem Conversions
		createTier2GemConverstionRecipies(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_LIKE_EXPORT), "dyeGreen");
		createTier2GemConverstionRecipies(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_REGULATE_EXPORT), "dyePurple");
		createTier2GemConverstionRecipies(new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SET_EXPORT), "dyeLightBlue");

		//Obelisk
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(obelisk), new Object[]{
				"VSV",
				"SCS",
				"VSV",
				'V', "dustVinteum",
				'S', "stone",
				'C', new ItemStack(Blocks.stonebrick, 1, 3)
		}));

		//Armor Infuser
		GameRegistry.addRecipe(new ItemStack(armorInfuser), new Object[]{
				"ACA",
				"OPO",
				"OOO",
				'A', BlocksCommonProxy.craftingAltar,
				'C', new ItemStack(Blocks.carpet, 1, Short.MAX_VALUE),
				'O', Blocks.obsidian,
				'P', Blocks.enchanting_table
		});

		//Spell Replicator
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(spellReplicator), new Object[]{
				"VDV",
				"WFW",
				"VDV",
				'W', new ItemStack(BlocksCommonProxy.magicWall),
				'V', "dustVinteum",
				'D', new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_COSMICDUST),
				'F', new ItemStack(ItemsCommonProxy.flickerJar, 1, Affinity.ARCANE.ordinal())
		}));

		//Entropic Enervator
		GameRegistry.addRecipe(new ItemStack(entropicEnervator), new Object[]{
				"GCG",
				"ORO",
				"OOO",
				'G', BlocksCommonProxy.goldInlay,
				'C', ItemsCommonProxy.deficitCrystal,
				'O', Blocks.obsidian,
				'R', Items.redstone
		});

		//storage blocks
		createStorageBlockRecipe(new ItemStack(AMOres, 1, BlockAMOre.META_MOONSTONE_BLOCK), new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_MOONSTONE));
		createStorageBlockRecipe(new ItemStack(AMOres, 1, BlockAMOre.META_VINTEUM_BLOCK), new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_VINTEUMDUST));
		createStorageBlockRecipe(new ItemStack(AMOres, 1, BlockAMOre.META_BLUE_TOPAZ_BLOCK), new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_BLUETOPAZ));
		createStorageBlockRecipe(new ItemStack(AMOres, 1, BlockAMOre.META_SUNSTONE_BLOCK), new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_SUNSTONE));
		createStorageBlockRecipe(new ItemStack(AMOres, 1, BlockAMOre.META_CHIMERITE_BLOCK), new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_CHIMERITE));

		//furnace recipes
		GameRegistry.addSmelting(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_ARCANECOMPOUND), new ItemStack(ItemsCommonProxy.itemOre, 2, ItemOre.META_ARCANEASH), 0);

		addMetaSmeltingRecipe(AMOres, BlockAMOre.META_VINTEUM_ORE, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_VINTEUMDUST));

		addMetaSmeltingRecipe(AMOres, BlockAMOre.META_SUNSTONE_ORE, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_SUNSTONE));
		addMetaSmeltingRecipe(AMOres, BlockAMOre.META_BLUE_TOPAZ_ORE, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_BLUETOPAZ));
		addMetaSmeltingRecipe(AMOres, BlockAMOre.META_CHIMERITE_ORE, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_CHIMERITE));
		addMetaSmeltingRecipe(AMOres, BlockAMOre.META_MOONSTONE_ORE, new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_MOONSTONE));

		OreDictionary.registerOre("oreBlueTopaz", new ItemStack(AMOres, 1, BlockAMOre.META_BLUE_TOPAZ_ORE));
		OreDictionary.registerOre("oreVinteum", new ItemStack(AMOres, 1, BlockAMOre.META_VINTEUM_ORE));
		OreDictionary.registerOre("oreChimerite", new ItemStack(AMOres, 1, BlockAMOre.META_CHIMERITE_ORE));
		OreDictionary.registerOre("oreMoonstone", new ItemStack(AMOres, 1, BlockAMOre.META_MOONSTONE_ORE));
		OreDictionary.registerOre("oreSunstone", new ItemStack(AMOres, 1, BlockAMOre.META_SUNSTONE_ORE));

		OreDictionary.registerOre("blockBlueTopaz", new ItemStack(AMOres, 1, BlockAMOre.META_BLUE_TOPAZ_BLOCK));
		OreDictionary.registerOre("blockVinteum", new ItemStack(AMOres, 1, BlockAMOre.META_VINTEUM_BLOCK));
		OreDictionary.registerOre("blockChimerite", new ItemStack(AMOres, 1, BlockAMOre.META_CHIMERITE_BLOCK));
		OreDictionary.registerOre("blockMoonstone", new ItemStack(AMOres, 1, BlockAMOre.META_MOONSTONE_BLOCK));
		OreDictionary.registerOre("blockSunstone", new ItemStack(AMOres, 1, BlockAMOre.META_SUNSTONE_BLOCK));

		OreDictionary.registerOre("chestWood", new ItemStack(Blocks.chest));
		OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.crafting_table));

		GameRegistry.addRecipe(new ItemStack(illusionBlock, illusionBlock.GetCraftingQuantity(), 0), illusionBlock.GetRecipeComponents(false));
		GameRegistry.addRecipe(new ItemStack(illusionBlock, illusionBlock.GetCraftingQuantity(), 1), illusionBlock.GetRecipeComponents(true));
	}

	private void addMetaSmeltingRecipe(Block input, int meta, ItemStack output){
		ItemStack stack = new ItemStack(input);
		stack.setItemDamage(meta);
		GameRegistry.addSmelting(stack, output, 0);
	}

	private void createStorageBlockRecipe(ItemStack storageBlock, ItemStack storageItem){
		GameRegistry.addRecipe(storageBlock, new Object[]{
				"III",
				"III",
				"III",
				'I', new ItemStack(storageItem.getItem(), 1, storageItem.getItemDamage())
		});

		GameRegistry.addShapelessRecipe(new ItemStack(storageItem.getItem(), 9, storageItem.getItemDamage()), storageBlock);
	}

	private void createTier2GemConverstionRecipies(ItemStack stack, String dyeCode){
		if (stack.getItemDamage() != BlockCrystalMarker.META_LIKE_EXPORT){
			GameRegistry.addRecipe(new ShapelessOreRecipe(stack, new Object[]{
					new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_LIKE_EXPORT),
					dyeCode
			}));
		}

		if (stack.getItemDamage() != BlockCrystalMarker.META_REGULATE_EXPORT){
			GameRegistry.addRecipe(new ShapelessOreRecipe(stack, new Object[]{
					new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_REGULATE_EXPORT),
					dyeCode
			}));
		}

		if (stack.getItemDamage() != BlockCrystalMarker.META_SET_EXPORT){
			GameRegistry.addRecipe(new ShapelessOreRecipe(stack, new Object[]{
					new ItemStack(crystalMarker, 1, BlockCrystalMarker.META_SET_EXPORT),
					dyeCode
			}));
		}
	}

	public void RegisterBlocks(){
		registerBlock(essenceRefiner, "essenceRefiner");
		registerBlock(blockMageTorch, "mageTorch");
		registerBlock(essenceConduit, "essenceConduit");
		registerBlock(obelisk, "obelisk");
		registerBlock(calefactor, "calefactor");
		registerBlock(caster, "blockCaster");
		registerBlock(casterRune, "CasterRune");
		registerBlock(keystoneRecepticle, "keystoneReceptacle");
		registerBlock(astralBarrier, "astralBarrier");
		registerBlock(blackAurem, "blackAurem");
		registerBlock(celestialPrism, "celestialPrism");
		registerBlock(seerStone, "seerStone");
		registerBlock(AMOres, OreItem.class, "vinteumOre");
		registerBlock(cerublossom, "blueOrchid");
		registerBlock(desertNova, "desertNova");
		registerBlock(keystoneChest, "keystoneChest");
		registerBlock(blockLectern, "blockLectern");
		registerBlock(manaBattery, "manaBattery");
		registerBlock(blockArcaneReconstructor, "blockArcaneReconstructor");
		registerBlock(magicWall, "magicWall");
		registerBlock(occulus, "occulus");
		registerBlock(craftingAltar, "CraftingAltar");
		registerBlock(spellRune, "SpellRune");
		registerBlock(vinteumTorch, "VinteumTorch");
		registerBlock(particleEmitter, "ParticleEmitter");
		registerBlock(inscriptionTable, "InscriptionTable");
		registerBlock(spatialVortex, "spatialVortex");
		registerBlock(invisibleUtility, "invisibleUtility");
		registerBlock(aum, "Aum");
		registerBlock(tarmaRoot, "TarmaRoot");
		registerBlock(effervescentSnowdrop, "EffervescentSnowdrop");
		registerBlock(sanguineAmaryllis, "SanguineAmaryllis");
		registerBlock(infusedStem, "InfusedStem");
		registerBlock(witchwoodLog, "WitchwoodLog");
		registerBlock(witchwoodLogDrained, "DrainedWitchwoodLog");
		registerBlock(witchwoodLeaves, "WitchwoodLeaves");
		registerBlock(summoner, "Summoner");
		registerBlock(liquidEssence, "liquidEssence");
		registerBlock(redstoneInlay, "redstoneInlay");
		registerBlock(ironInlay, "ironInlay");
		registerBlock(goldInlay, "goldInlay");
		registerBlock(witchwoodPlanks, "planksWitchwood");
		registerBlock(witchwoodStairs, "stairsWitchwood");
		registerBlock(witchwoodSapling, "saplingWitchwood");
		registerBlock(magiciansWorkbench, "magiciansWorkbench");
		registerBlock(everstone, "everstone");
		registerBlock(keystoneDoor, "keystoneDoor");
		registerBlock(keystoneTrapDoor, "keystoneTrapDoor");
		registerBlock(slipstreamGenerator, "slipstreamGenerator");
		registerBlock(elementalAttuner, "elementalAttuner");
		registerBlock(wizardChalk, "wizardChalk");
		registerBlock(inertSpawner, "inertSpawner");
		registerBlock(candle, "candle");
		registerBlock(brokenLinkBlock, "brokenLinkBlock");
		registerBlock(armorInfuser, "armorInfuser");
		registerBlock(flickerLure, "flickerLure");
		registerBlock(arcaneDeconstructor, "arcaneDeconstructor");
		registerBlock(wakebloom, "wakebloom");
		registerBlock(otherworldAura, "otherworldAura");
		registerBlock(spellSealedDoor, "spellSealedDoor");
		registerBlock(entropicEnervator, "entropicEvervator");
		registerBlock(spellReplicator, "spellReplicator");

		registerBlock(rainRockrose, "rainRockrose");
		registerBlock(stormSawtooth, "stormSawtooth");
		registerBlock(imbuedMoonflower, "imbuedMoonflower");

		//if you need a special item placer for the block, set it here instead of registering the block normally above
		/*Item.itemsList[BlocksCommonProxy.witchwoodSingleSlab.blockID] = new ItemSlab(BlocksCommonProxy.witchwoodSingleSlab.blockID - 256, BlocksCommonProxy.witchwoodSingleSlab, BlocksCommonProxy.witchwoodDoubleSlab, false);
		Item.itemsList[BlocksCommonProxy.witchwoodDoubleSlab.blockID] = new ItemSlab(BlocksCommonProxy.witchwoodDoubleSlab.blockID - 256, BlocksCommonProxy.witchwoodSingleSlab, BlocksCommonProxy.witchwoodDoubleSlab, true);
		Item.itemsList[BlocksCommonProxy.illusionBlock.blockID] = (new ItemMultiTextureTile(BlocksCommonProxy.illusionBlock.blockID - 256, BlocksCommonProxy.illusionBlock, IllusionBlock.illusion_block_types)).setUnlocalizedName("arsmagica2:illusionBlock");
		Item.itemsList[BlocksCommonProxy.crystalMarker.blockID] = (new ItemMultiTextureTile(BlocksCommonProxy.crystalMarker.blockID - 256, BlocksCommonProxy.crystalMarker, BlockCrystalMarker.crystalMarkerTypes)).setUnlocalizedName("arsmagica2:crystalMarker");*/
		registerMultiTextureBlock(witchwoodSingleSlab, "witchwoodSingleSlab", new ItemSlab(witchwoodSingleSlab, witchwoodSingleSlab, witchwoodDoubleSlab, false));
		registerMultiTextureBlock(witchwoodDoubleSlab, "witchwoodDoubleSlab", new ItemSlab(witchwoodDoubleSlab, witchwoodSingleSlab, witchwoodDoubleSlab, false));
		registerMultiTextureBlock(illusionBlock, "illusionBlock", new ItemMultiTexture(illusionBlock, illusionBlock, IllusionBlock.illusion_block_types).setUnlocalizedName("arsmagica2:illusionBlock"));
		registerMultiTextureBlock(crystalMarker, "crystalMarker", new ItemMultiTexture(crystalMarker, crystalMarker, BlockCrystalMarker.crystalMarkerTypes).setUnlocalizedName("arsmagica2:crystalMarker"));

		arsMagicaBlocksList.add(illusionBlock);
		arsMagicaBlocksList.add(crystalMarker);
		arsMagicaBlocksList.add(witchwoodSingleSlab);
		arsMagicaBlocksList.add(witchwoodDoubleSlab);

		OreDictionary.registerOre("logWood", witchwoodLog);
		OreDictionary.registerOre("stairWood", witchwoodStairs);
		OreDictionary.registerOre("plankWood", witchwoodPlanks);
		OreDictionary.registerOre("slabWood", witchwoodSingleSlab);
		OreDictionary.registerOre("plankWitchwood", witchwoodPlanks);
		OreDictionary.registerOre("witchwoodLog", witchwoodLog);
		OreDictionary.registerOre("stairWitchwood", witchwoodStairs);
		OreDictionary.registerOre("slabWitchwood", witchwoodSingleSlab);
		OreDictionary.registerOre("treeLeaves", witchwoodLeaves);
		OreDictionary.registerOre("treeSapling", witchwoodSapling);
	}

	private <T extends ItemBlock> void registerMultiTextureBlock(Block block, String unlocalizedName, T item){
		arsMagicaBlocksList.add(block);
		try{
			GameData data = ReflectionHelper.getPrivateValue(GameData.class, null, "mainData");
			Method registerBlock = ReflectionHelper.findMethod(GameData.class, data, new String[]{"registerBlock"}, Block.class, String.class);
			Method registerItem = ReflectionHelper.findMethod(GameData.class, data, new String[]{"registerItem"}, Item.class, String.class);
			//register block first
			registerBlock.invoke(data, block, unlocalizedName);
			//register item second
			registerItem.invoke(data, item, unlocalizedName);
		}catch (Throwable t){
			t.printStackTrace();
		}
	}

	private void registerBlock(Block block, String name){
		arsMagicaBlocksList.add(block);
		GameRegistry.registerBlock(block, name);
	}

	private void registerBlock(Block block, Class placerClass, String name){
		arsMagicaBlocksList.add(block);
		GameRegistry.registerBlock(block, placerClass, name);
	}

	public void RegisterTileEntities(){
		GameRegistry.registerTileEntity(TileEntityEssenceRefiner.class, "TileEntityEssenceRefiner");
		GameRegistry.registerTileEntity(TileEntityEssenceConduit.class, "TileEntityEssenceConduit");
		GameRegistry.registerTileEntity(TileEntityObelisk.class, "TileEntityObelisk");
		GameRegistry.registerTileEntity(TileEntityBlockCaster.class, "TileEntityBlockCaster");
		GameRegistry.registerTileEntity(TileEntityCasterRune.class, "TileEntityCasterRune");
		GameRegistry.registerTileEntity(TileEntityCalefactor.class, "TileEntityCalefactor");
		GameRegistry.registerTileEntity(TileEntityKeystoneRecepticle.class, "TileEntityKeystoneRecepticle");
		GameRegistry.registerTileEntity(TileEntityAstralBarrier.class, "TileEntityAstralBarrier");
		GameRegistry.registerTileEntity(TileEntityBlackAurem.class, "TileEntityBlackAurem");
		GameRegistry.registerTileEntity(TileEntitySpatialVortex.class, "TileEntitySpatialVortex");
		GameRegistry.registerTileEntity(TileEntitySeerStone.class, "TileEntitySeerStone");
		GameRegistry.registerTileEntity(TileEntityCelestialPrism.class, "TileEntityCelestialPrism");
		GameRegistry.registerTileEntity(TileEntityKeystoneChest.class, "TileEntityKeystoneChest");
		GameRegistry.registerTileEntity(TileEntityLectern.class, "TileEntityBlockLectern");
		GameRegistry.registerTileEntity(TileEntityArcaneReconstructor.class, "TileEntityArcaneReconstructor");
		GameRegistry.registerTileEntity(TileEntityManaBattery.class, "TileEntityManaBattery");
		GameRegistry.registerTileEntity(TileEntityOcculus.class, "TileEntityOcculus");
		GameRegistry.registerTileEntity(TileEntityCraftingAltar.class, "TileEntityCraftingAltar");
		GameRegistry.registerTileEntity(TileEntityGroundRuneSpell.class, "TileEntityGroundRuneSpell");
		GameRegistry.registerTileEntity(TileEntityParticleEmitter.class, "TileEntityParticleEmitter");
		GameRegistry.registerTileEntity(TileEntityInscriptionTable.class, "TileEntityInscriptionTable");
		GameRegistry.registerTileEntity(TileEntitySummoner.class, "TileEntitySummoner");
		GameRegistry.registerTileEntity(TileEntityMagiciansWorkbench.class, "TileEntityMagiciansWorkbench");
		GameRegistry.registerTileEntity(TileEntityEverstone.class, "TileEntityEverstone");
		GameRegistry.registerTileEntity(TileEntityKeystoneDoor.class, "TileEntityKeystoneDoor");
		GameRegistry.registerTileEntity(TileEntitySlipstreamGenerator.class, "TileEntitySlipstreamGenerator");
		GameRegistry.registerTileEntity(TileEntityCrystalMarker.class, "TileEntityCrystalMarker");
		GameRegistry.registerTileEntity(TileEntityFlickerHabitat.class, "TileEntityElementalAttuner");
		GameRegistry.registerTileEntity(TileEntityCandle.class, "TileEntityCandle");
		GameRegistry.registerTileEntity(TileEntityBrokenPowerLink.class, "TileEntityBrokenPowerLink");
		GameRegistry.registerTileEntity(TileEntityArmorImbuer.class, "TileEntityArmorInfuser");
		GameRegistry.registerTileEntity(TileEntityFlickerLure.class, "TileEntityFlickerLure");
		GameRegistry.registerTileEntity(TileEntityArcaneDeconstructor.class, "TileEntityArcaneDeconstructor");
		GameRegistry.registerTileEntity(TileEntityOtherworldAura.class, "TileEntityOtherworldAura");
		GameRegistry.registerTileEntity(TileEntityCrystalMarkerSpellExport.class, "TileEntityCrystalMarkerSpellExport");
		GameRegistry.registerTileEntity(TileEntityInertSpawner.class, "TileEntityInertSpawner");
		GameRegistry.registerTileEntity(TileEntitySpellSealedDoor.class, "TileEntitySpellSealedDoor");
		GameRegistry.registerTileEntity(TileEntityEntropicEnervator.class, "TileEntityEntropicEnervator");
		GameRegistry.registerTileEntity(TileEntitySpellReplicator.class, "TileEntitySpellReplicator");
		GameRegistry.registerTileEntity(TileEntityInfusedStem.class, "TileEntityInfusedStem");
	}

	public void registerRenderInformation(){
	}
}
