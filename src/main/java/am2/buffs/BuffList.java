package am2.buffs;

import am2.AMCore;
import am2.LogHelper;
import am2.api.potion.IBuffHelper;
import am2.particles.AMParticle;
import am2.particles.ParticleLiveForBuffDuration;
import am2.texture.ResourceManager;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuffList implements IBuffHelper{
	//buff "potions"
	public static ArsMagicaPotion waterBreathing;
	public static ArsMagicaPotion flight;
	public static ArsMagicaPotion slowfall;
	public static ArsMagicaPotion haste;
	public static ArsMagicaPotion trueSight;
	public static ArsMagicaPotion regeneration;
	public static ArsMagicaPotion magicShield;
	public static ArsMagicaPotion charmed;
	public static ArsMagicaPotion frostSlowed;
	public static ArsMagicaPotion temporalAnchor;
	public static ArsMagicaPotion manaRegen;
	public static ArsMagicaPotion entangled;
	public static ArsMagicaPotion wateryGrave;
	public static ArsMagicaPotion spellReflect;
	public static ArsMagicaPotion silence;
	public static ArsMagicaPotion swiftSwim;
	public static ArsMagicaPotion agility;
	public static ArsMagicaPotion leap;
	public static ArsMagicaPotion gravityWell;
	public static ArsMagicaPotion astralDistortion;
	public static ArsMagicaPotion levitation;
	public static ArsMagicaPotion clarity;
	public static ArsMagicaPotion illumination;
	public static ArsMagicaPotion manaBoost;
	public static ArsMagicaPotion manaShield;
	public static ArsMagicaPotion fury;
	public static ArsMagicaPotion scrambleSynapses;
	public static ArsMagicaPotion shrink;
	public static ArsMagicaPotion burnoutReduction;
	public static ArsMagicaPotion psychedelic;

	public static ArsMagicaPotion greaterManaPotion;
	public static ArsMagicaPotion epicManaPotion;
	public static ArsMagicaPotion legendaryManaPotion;

	private static final int maxParticlesPerBuff = 100;
	public static final int default_buff_duration = 600;
	private static int potionDefaultOffset = 0;
	public static HashMap<Integer, Integer> particlesForBuffID;
	private static HashMap<Integer, Class<? extends BuffEffect>> classesForBuffID;

	//since sync code only applies the PotionEffect class, any inherited bonuses are lost.
	//These dummy buffs can be used to apply the tick of the potion when the class type
	//isn't transferred.
	private static final HashMap<Integer, BuffEffect> utilityBuffs = new HashMap<Integer, BuffEffect>();
	private static final ArrayList<ArsMagicaPotion> arsMagicaPotions = new ArrayList<ArsMagicaPotion>();
	private static ArrayList<Integer> dispelBlacklist;
	

	public static final BuffList instance = new BuffList();

	private static HashMap<Integer, Potion> ourInitialPotionAllocations;

	private BuffList(){

	}

	private static ArsMagicaPotion createAMPotion(int index, String name, int IIconRow, int iconCol, boolean isBadEffect, Class<? extends BuffEffect> buffEffectClass){

		String configID = name.replace(" ", "").toLowerCase().trim();
		index = AMCore.config.getConfigurablePotionID(configID, index);

		LogHelper.info("Potion %s is ID %d", name, index);
		
		if (Potion.potionTypes[index] != null){
			LogHelper.error("Warning: Potion index %d is already occupied by potion %s. Check your config files for clashes.", index, Potion.potionTypes[index].getName());
		}

		ArsMagicaPotion potion = new ArsMagicaPotion(index, isBadEffect, 0x000000);
		potion.setPotionName(name);
		potion._setIconIndex(iconCol, IIconRow);
		classesForBuffID.put(index, buffEffectClass);
		arsMagicaPotions.add(potion);
		ourInitialPotionAllocations.put(index, potion);
		
		return potion;
	}

	private static ManaPotion createManaPotion(int index, String name, int IIconRow, int iconCol, boolean isBadEffect, int colour){
		String configID = name.replace(" ", "").toLowerCase().trim();
		index = AMCore.config.getConfigurablePotionID(configID, index);
		
		LogHelper.info("Potion %s is ID %d", name, index);
		
		if (Potion.potionTypes[index] != null){
			LogHelper.warn("Warning: Potion index %d is already occupied by potion %s. Check your config files for clashes.", index, Potion.potionTypes[index].getName());
		}
		
		ManaPotion potion = new ManaPotion(index, isBadEffect, colour);
		potion.setPotionName(name);
		potion._setIconIndex(iconCol, IIconRow);
		ourInitialPotionAllocations.put(index, potion);
		
		return potion;
	}

	private static void createDummyBuff(Class<? extends  BuffEffect> buffEffectClass, int potionID){
		try{
			Constructor ctor = buffEffectClass.getConstructor(Integer.TYPE, Integer.TYPE);
			BuffEffect utilityBuff = (BuffEffect)ctor.newInstance(0, 0);
			utilityBuffs.put(potionID, utilityBuff);
		}catch (Throwable e){
			e.printStackTrace();
		}
	}

	public static void Init(){
		dispelBlacklist = new ArrayList<Integer>();
		particlesForBuffID = new HashMap<Integer, Integer>();
		classesForBuffID = new HashMap<Integer, Class<? extends BuffEffect>>();
		ourInitialPotionAllocations = new HashMap<Integer, Potion>();

		try{
			extendPotionsArray();

			int numBuffs = Potion.potionTypes.length;

			for (int i = 0; i < numBuffs; ++i){
				particlesForBuffID.put(i, 0);
			}
		}catch (Throwable t){
			LogHelper.error("Buffs failed to initialize!  This will make the game very unstable!");
			t.printStackTrace();
		}
	}

	public static void postInit(){
		// potion clash detection
		for (Map.Entry<Integer, Potion> entry : ourInitialPotionAllocations.entrySet()){
			if (Potion.potionTypes[entry.getKey()] != entry.getValue()){
				LogHelper.warn("My potion, %s, at index %d has been over-written by another potion, %s. You have a conflict in your configuration files.", entry.getValue().getName(), entry.getKey(), Potion.potionTypes[entry.getKey()].getName());
			}
		}
	}

	public static void Instantiate(){
		waterBreathing = createAMPotion(potionDefaultOffset + 0, "Water Breathing", 0, 0, false, BuffEffectWaterBreathing.class);
		flight = createAMPotion(potionDefaultOffset + 1, "Flight", 0, 1, false, BuffEffectFlight.class);
		slowfall = createAMPotion(potionDefaultOffset + 2, "Feather Fall", 0, 2, false, BuffEffectSlowfall.class);
		haste = createAMPotion(potionDefaultOffset + 3, "Haste", 0, 3, false, BuffEffectHaste.class);
		trueSight = createAMPotion(potionDefaultOffset + 4, "True Sight", 0, 4, false, BuffEffectTrueSight.class);
		regeneration = createAMPotion(potionDefaultOffset + 5, "Regeneration", 0, 6, false, BuffEffectRegeneration.class);
		magicShield = createAMPotion(potionDefaultOffset + 6, "Magic Shield", 1, 1, false, BuffEffectMagicShield.class);
		charmed = createAMPotion(potionDefaultOffset + 7, "Charmed", 1, 2, true, BuffEffectCharmed.class);
		frostSlowed = createAMPotion(potionDefaultOffset + 8, "Frost Slow", 1, 3, true, BuffEffectFrostSlowed.class);
		temporalAnchor = createAMPotion(potionDefaultOffset + 9, "Chrono Anchor", 1, 4, false, BuffEffectTemporalAnchor.class);
		manaRegen = createAMPotion(potionDefaultOffset + 10, "Mana Regen", 1, 5, false, BuffEffectManaRegen.class);
		entangled = createAMPotion(potionDefaultOffset + 11, "Entangled", 1, 7, true, BuffEffectEntangled.class);
		wateryGrave = createAMPotion(potionDefaultOffset + 12, "Watery Grave", 2, 0, true, BuffEffectWateryGrave.class);
		spellReflect = createAMPotion(potionDefaultOffset + 13, "Spell Reflect", 2, 3, false, BuffEffectSpellReflect.class);
		silence = createAMPotion(potionDefaultOffset + 14, "Silence", 2, 6, true, BuffEffectSilence.class);
		swiftSwim = createAMPotion(potionDefaultOffset + 15, "Swift Swim", 2, 7, false, BuffEffectSwiftSwim.class);
		agility = createAMPotion(potionDefaultOffset + 16, "Agility", 0, 0, false, BuffEffectAgility.class);
		leap = createAMPotion(potionDefaultOffset + 17, "Leap", 0, 2, false, BuffEffectLeap.class);
		manaBoost = createAMPotion(potionDefaultOffset + 18, "Mana Boost", 1, 0, false, BuffMaxManaIncrease.class);
		astralDistortion = createAMPotion(potionDefaultOffset + 19, "Astral Distortion", 0, 4, true, BuffEffectAstralDistortion.class);
		manaShield = createAMPotion(potionDefaultOffset + 20, "Mana Shield", 0, 7, false, BuffEffectManaShield.class);
		fury = createAMPotion(potionDefaultOffset + 21, "Fury", 1, 6, false, BuffEffectFury.class);
		scrambleSynapses = createAMPotion(potionDefaultOffset + 22, "Scramble Synapses", 2, 1, true, BuffEffectScrambleSynapses.class);
		illumination = createAMPotion(potionDefaultOffset + 23, "Illuminated", 1, 0, false, BuffEffectIllumination.class);
		
		greaterManaPotion = createManaPotion(potionDefaultOffset + 24, "Greater Mana Restoration", 0, 1, false, 0x40c6be);
		// greaterManaPotion = new ManaPotion(potionDefaultOffset + 24, false, 0x40c6be);
		// greaterManaPotion.setPotionName("Greater Mana Restoration");
		// greaterManaPotion._setIconIndex(0, 1);

		epicManaPotion = createManaPotion(potionDefaultOffset + 25, "Epic Mana Restoration", 0, 1, false, 0xFF00FF);
		// epicManaPotion = new ManaPotion(potionDefaultOffset + 25, false, 0xFF00FF);
		// epicManaPotion.setPotionName("Epic Mana Restoration");
		// epicManaPotion._setIconIndex(0, 1);

		legendaryManaPotion = createManaPotion(potionDefaultOffset + 26, "Legendary Mana Restoration", 0, 1, false, 0xFFFF00);
		// legendaryManaPotion = new ManaPotion(potionDefaultOffset + 26, false, 0xFFFF00);
		// legendaryManaPotion.setPotionName("Legendary Mana Restoration");
		// legendaryManaPotion._setIconIndex(0, 1);

		gravityWell = createAMPotion(potionDefaultOffset + 27, "Gravity Well", 0, 6, true, BuffEffectGravityWell.class);
		levitation = createAMPotion(potionDefaultOffset + 28, "Levitation", 0, 7, false, BuffEffectLevitation.class);

		clarity = createAMPotion(potionDefaultOffset + 29, "Clarity", 0, 5, false, BuffEffectClarity.class);
		shrink = createAMPotion(potionDefaultOffset + 30, "Shrunken", 0, 5, false, BuffEffectShrink.class);
		burnoutReduction = createAMPotion(potionDefaultOffset + 31, "Burnout Redux", 1, 1, false, BuffEffectBurnoutReduction.class);
		psychedelic = createAMPotion(potionDefaultOffset + 32, "Psychedelic", 1, 2, false, BuffEffectPsychedelic.class);

		for (int i : classesForBuffID.keySet()){
			createDummyBuff(classesForBuffID.get(i), i);
		}
	}

	public static void setupTextureOverrides(){
		waterBreathing.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		flight.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		slowfall.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		haste.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		trueSight.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		regeneration.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		magicShield.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		charmed.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		frostSlowed.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		temporalAnchor.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		manaRegen.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		entangled.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		wateryGrave.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		;
		spellReflect.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		silence.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		swiftSwim.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		clarity.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		manaShield.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		manaBoost.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		fury.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));

		agility.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		leap.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		astralDistortion.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		gravityWell.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		levitation.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		illumination.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		scrambleSynapses.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_1.png"));
		shrink.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		burnoutReduction.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
		psychedelic.setTextureSheet(ResourceManager.GetGuiTexturePath("buffs_2.png"));
	}

	/*
	 * This is bad.  Don't ever use this unless you absolutely HAVE to!
	 * The only reason I did was to remove the need to edit base classes.  You can REALLY mess up a program with this if not used properly...
	 * Technically, you could change false to true.  I'm not even kidding.  The actual keyword would have a different value.  BE CAREFUL!
	 */
	private static void setFinalStatic(Field field, Object newValue) throws Exception{
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}

	private static void extendPotionsArray() throws Exception{
		LogHelper.info("Extending potions array");
		LogHelper.info("Injecting potions starting from index " + Potion.potionTypes.length);
		potionDefaultOffset = Potion.potionTypes.length;
		setPotionArrayLength(255);
	}

	private static void setPotionArrayLength(int length) throws Exception{
		if (length <= Potion.potionTypes.length)
			return;
		Potion[] potions = new Potion[length];
		for (int i = 0; i < Potion.potionTypes.length; ++i){
			potions[i] = Potion.potionTypes[i];
		}
		Field field = null;
		Field[] fields = Potion.class.getDeclaredFields();
		for (Field f : fields){
			if (f.getType().equals(Potion[].class)){
				field = f;
				break;
			}
		}
		setFinalStatic(field, potions);
	}

	public static boolean IDIsAMBuff(int potionID){
		for (ArsMagicaPotion i : arsMagicaPotions){
			if (i.id == potionID){
				return true;
			}
		}
		return false;
	}

	public static boolean addParticleToBuff(AMParticle particle, EntityLiving ent, int priority, boolean exclusive, int buffID){
		if (particlesForBuffID.get(buffID) >= maxParticlesPerBuff){
			return false;
		}
		int count = particlesForBuffID.get(buffID);
		count++;
		particlesForBuffID.put(buffID, count);
		particle.AddParticleController(new ParticleLiveForBuffDuration(particle, ent, buffID, priority, exclusive));
		return true;
	}

	public static BuffEffect buffEffectFromPotionID(int potionID, int duration, int amplifier){
		Class<? extends  BuffEffect> _class = classesForBuffID.get(potionID);
		if (_class == null) return null;

		Constructor buffMaker = _class.getDeclaredConstructors()[0];
		try{
			buffMaker.setAccessible(true);
			BuffEffect p = (BuffEffect)buffMaker.newInstance(duration, amplifier);
			return p;
		}catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			LogHelper.error("Could not create potion: " + e.getMessage());
		}
		return null;
	}

	public static void buffEnding(int buffID){
		particlesForBuffID.put(buffID, 0);
	}

	@Override
	public int getPotionID(String name){
		Field potionField = ReflectionHelper.findField(BuffList.class, name);
		if (potionField != null && potionField.getType() == ArsMagicaPotion.class){
			try{
				return ((ArsMagicaPotion)potionField.get(null)).getId();
			}catch (IllegalArgumentException e){
				e.printStackTrace();
			}catch (IllegalAccessException e){
				e.printStackTrace();
			}
		}
		return -1;
	}

	@Override
	public void addDispelExclusion(int id){
		if (dispelBlacklist.contains(id)){
			LogHelper.info("Id %d was already on the dispel blacklist; skipping.", id);
		}else{
			LogHelper.info("Added %d to the dispel blacklist.", id);
			dispelBlacklist.add(id);
		}
	}

	public static boolean isDispelBlacklisted(int id){
		return dispelBlacklist.contains(id);
	}
}
