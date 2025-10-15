package am2.lore;

import am2.AMCore;
import am2.LogHelper;
import am2.api.ILoreHelper;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.items.ItemsCommonProxy;
import am2.playerextensions.ExtendedProperties;
import am2.proxy.tick.ClientTickHandler;
import am2.utility.WebRequestUtils;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.Language;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

@SideOnly(Side.CLIENT)
public class ArcaneCompendium implements ILoreHelper{

	public static final ArcaneCompendium instance = new ArcaneCompendium();

	public static final String KEYWORD_NEWPAGE = "!p";
	public static final String KEYWORD_NEWLINE = "!l";
	public static final String KEYWORD_DOUBLENEWLINE = "!d";

	public static Achievement compendiumData = (new Achievement("am2_ach_data", "compendiumData", 0, 0, ItemsCommonProxy.arcaneCompendium, null));
	public static Achievement componentUnlock = (new Achievement("am2_ach_unlock", "componentUnlock", 0, 0, ItemsCommonProxy.spellParchment, null));

	private final TreeMap<String, CompendiumEntry> compendium;
	private final TreeMap<String, String> aliases;
	private final TreeMap<String, String> zeroItemTexts;

	private boolean hasLoaded = false;

	private String languageCode = "";
	private String MCVersion = "";

	private ArcaneCompendium(){
		compendium = new TreeMap<String, CompendiumEntry>();
		aliases = new TreeMap<String, String>();
		zeroItemTexts = new TreeMap<String, String>();
	}

	private String getWorldName(){
		if (ClientTickHandler.worldName == null)
			return null;
		return ClientTickHandler.worldName.replaceAll("[^\\x00-\\x7F]", "");
	}

	public void saveUnlockData(){
		if (!hasLoaded)
			return;

		try{
			EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
			if(player != null){
				ExtendedProperties props = ExtendedProperties.For(player);
				if (props != null){
					System.out.println("Saving Unlock Data!");
					for (CompendiumEntry entry : compendium.values()){
						String id = "U";
						if (entry.isLocked)
							id += "L";
						if (entry.isNew)
							id += "N";
						props.setCompendiumEntry(entry.getID(), id);
					}
				}
			}
		}catch (Exception e){
			LogHelper.error("Compendium unlock state failed to save! Please report this error!");
			e.printStackTrace();
		}
	}

	public void loadUnlockData(){
		if (!AMCore.config.stagedCompendium()){
			for (CompendiumEntry entry : compendium.values()){
				entry.isLocked = false;
				entry.isNew = false;
			}
			return;
		}

		try{
			if (Minecraft.getMinecraft().thePlayer != null && ExtendedProperties.For(Minecraft.getMinecraft().thePlayer) != null) {
				hasLoaded = true;
				System.out.println("Loading Unlock Data!");
				ExtendedProperties.For(Minecraft.getMinecraft().thePlayer).requestEntriesUpdateFromServer();
			}
		}catch (Exception e){
			LogHelper.error("Compendium unlock state failed to load! Please report this error!");
			e.printStackTrace();
		}
	}

	public void addAlias(String alias, String baseItem){
		aliases.put(alias, baseItem);
	}

	public void init(Language lang){
		if (lang == null){
			LogHelper.error("Got a current language of NULL from Minecraft?!?  The compendium cannot load!");
			return;
		}
		MCVersion = (String)ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110447_Z", "launchedVersion");
		languageCode = lang.getLanguageCode().trim();

		compendium.clear();
		aliases.clear();
		zeroItemTexts.clear();

		//get the compendium stream again, either from an updated version or the default packaged one
		InputStream stream = getCompendium(lang);
		if (stream == null){
			LogHelper.error("Unable to load the Arcane Compendium!");
			return;
		}
		//load the entire document
		loadDocument(stream);
		//cleanup
		try{
			stream.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Gets the entry, with alias checks.
	 */
	public CompendiumEntry getEntry(String key){
		if (aliases.containsKey(key)){
			CompendiumEntry aliased = compendium.get(aliases.get(key));
			if (aliased.hasSubItems()){
				for (CompendiumEntry sub : aliased.getSubItems()){
					if (sub.getID().equals(key)){
						return sub;
					}
				}
			}
			return aliased;
		}
		return compendium.get(key);
	}

	/**
	 * Gets the entry without alias checks.
	 */
	public CompendiumEntry getEntryAbsolute(String key){
		return compendium.get(key);
	}

	public String getZeroItemText(String category){
		String s = zeroItemTexts.get(category);
		return s != null ? s : "";
	}

	private void loadDocument(InputStream stream){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(stream);

			Node parentNode = document.getChildNodes().item(0);

			NodeList categories = parentNode.getChildNodes();

			for (int i = 0; i < categories.getLength(); ++i){
				Node category = categories.item(i);

				if (category.getAttributes() != null){
					Node zeroItemText = category.getAttributes().getNamedItem("zeroItemText");
					if (zeroItemText != null){
						zeroItemTexts.put(category.getNodeName(), zeroItemText.getTextContent());
					}
				}

				NodeList entries = category.getChildNodes();
				for (int j = 0; j < entries.getLength(); ++j){
					Node entry = entries.item(j);
					CompendiumEntryType type = CompendiumEntryTypes.getForSection(category.getNodeName(), entry.getNodeName());
					if (type == null) continue;
					CompendiumEntry ce = type.createCompendiumEntry(entry);
					if (ce == null) continue;
					this.compendium.put(ce.getID(), ce);
				}
			}

		}catch (Throwable t){
			t.printStackTrace();
		}
	}

	// utility function, might come in handy later
	public Integer versionCompare(String str1, String str2){
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])){
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length){
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		else{
			return Integer.signum(vals1.length - vals2.length);
		}
	}

	private InputStream getCompendium(Language lang){
		return getPackagedCompendium(lang);
	}

	private InputStream getPackagedCompendium(Language lang){
		ResourceLocation rLoc = new ResourceLocation("arsmagica2", String.format("docs/ArcaneCompendium_%s.xml", lang.getLanguageCode()));
		IResource resource = null;
		try{
			resource = Minecraft.getMinecraft().getResourceManager().getResource(rLoc);
		}catch (IOException e){
		}finally{
			if (resource == null){
				LogHelper.info("Unable to find localized compendium.  Defaulting to en_US");
				rLoc = new ResourceLocation("arsmagica2", "docs/ArcaneCompendium_en_US.xml");
				try{
					resource = Minecraft.getMinecraft().getResourceManager().getResource(rLoc);
				}catch (IOException e){
				}
			}
		}

		if (resource != null)
			return resource.getInputStream();

		throw new MissingResourceException("No packaged version of the compendium was found.  You may have a corrupted download.", "compendium", "Arcane Compendium");
	}

	public LinkedHashSet<CompendiumEntryType> getCategories(){
		LinkedHashSet<CompendiumEntryType> toReturn = new LinkedHashSet();
		for (CompendiumEntryType type : CompendiumEntryTypes.categoryList()){
			toReturn.add(type);
		}
		return toReturn;
	}

	public ArrayList<CompendiumEntry> getEntriesForCategory(String category){
		ArrayList<CompendiumEntry> toReturn = new ArrayList<CompendiumEntry>();

		for (CompendiumEntry entry : compendium.values()){
			if (entry.type.getCategoryName().equals(category)){
				toReturn.add(entry);
			}
		}

		Collections.sort(toReturn);

		return toReturn;
	}

	private void unlockEntry(CompendiumEntry entry, boolean unlockRelated){
		if (unlockRelated){
			for (CompendiumEntry e : entry.getRelatedItems()){
				unlockEntry(e, false);
			}

			for (CompendiumEntry e : entry.getSubItems()){
				unlockEntry(e, false);
			}
		}
		if (entry.isLocked()){
			entry.setIsLocked(false);
			if (ExtendedProperties.For(Minecraft.getMinecraft().thePlayer).getMagicLevel() > 0)
				Minecraft.getMinecraft().guiAchievement.func_146256_a(compendiumData);
			saveUnlockData();
		}
	}

	/**
	 * Unlocks a Compendium Entry and all of it's related entries
	 *
	 * @param key The key used to identify the entry to unlock
	 */
	public void unlockEntry(String key){
		boolean verbose = key.startsWith("cmd::");
		key = key.replace("cmd::", "");

		if (verbose && key.equals("all")){
			for (CompendiumEntry entry : compendium.values()){
				entry.setIsLocked(false);
			}
			saveUnlockData();
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.unlockSuccess")));
			return;
		}

		CompendiumEntry entry = getEntry(key);
		if (entry == null){
			if (key.contains("@")){
				entry = getEntry(key.split("@")[0]);
			}
			if (entry == null){
				LogHelper.warn("Attempted to unlock a compendium entry for a non-existant key: " + key);
				if (verbose){
					String message = String.format(StatCollector.translateToLocal("am2.tooltip.compEntryNotFound"), key);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
				}
				return;
			}
		}
		while (entry.getParent() != null)
			entry = entry.getParent();
		if (entry.isLocked){
			unlockEntry(entry, true);
			if (verbose){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("am2.tooltip.unlockSuccess"));
			}
		}
	}

	public void unlockCategory(String category){
		for (CompendiumEntry entry : compendium.values()){
			if (entry.type.getCategoryName().equals(category)){
				unlockEntry(entry, false);
			}
		}
	}

	public void unlockRelatedItems(ItemStack stack){
		//initial error checking
		if (stack == null)
			return;

		//initialize variables (definitely don't miss the C days...)
		//initialize not at the top of the function.  Take that, old school!
		CompendiumEntry entry = null;
		String itemID = "";

		//ensure the unlocalized name has been set
		if (stack.getItem().getUnlocalizedName() == null)
			return;

		//construct a search ID based on what is passed in
		if (stack.getItem() instanceof Item){
			itemID = stack.getItem().getUnlocalizedName().replace("item.", "").replace("arsmagica2:", "");
		}else if (stack.getItem() instanceof ItemBlock){
			itemID = stack.getItem().getUnlocalizedName().replace("arsmagica2:", "").replace("tile.", "");
		}

		//append meta specific search if needed
		if (stack.getItemDamage() > -1)
			itemID += "@" + stack.getItemDamage();

		//search based on our constructed ID
		entry = getEntry(itemID);

		//did we find something?
		if (entry != null){
			//great!  ensure it's unlocked in the compendium
			unlockEntry(entry, true);
		}
	}

	public void setLockedState(boolean b){
		for (CompendiumEntry entry : this.compendium.values())
			entry.setIsLocked(b);
		saveUnlockData();
	}

	public boolean isCategory(String id){
		for (CompendiumEntryType type : this.getCategories())
			if (type.getCategoryName().equals(id))
				return true;
		return false;
	}

	@Override
	public void AddCompenidumEntry(Object entryItem, String entryKey, String entryName, String entryDesc, String parent, boolean allowReplace, String... relatedKeys){
		if (entryItem == null){
			LogHelper.warn("Null entry item passed.  Cannot add Compendium Entry with key %s.", entryKey);
			return;
		}

		CompendiumEntry existingEntry = getEntry(entryKey);
		if (existingEntry != null && !allowReplace){
			LogHelper.warn("Compendium entry with key %s exists, and allowReplace is false.  The entry was not added.", entryKey);
			return;
		}

		CompendiumEntry parentEntry = parent == null ? null : getEntry(parent);
		if (parent != null && parentEntry == null){
			LogHelper.warn("The parent ID %s was not found.  Entry %s will be added with no parent.", parent, entryKey);
		}

		CompendiumEntry newEntry = getCompendiumEntry(entryItem);

		newEntry.id = entryKey;
		newEntry.name = entryName;
		newEntry.description = entryDesc;
		newEntry.isLocked = true;
		newEntry.isNew = true;
		newEntry.parent = parentEntry;
		newEntry.relatedItems.addAll(Arrays.asList(relatedKeys));

		if (parentEntry != null){
			parentEntry.subItems.add(newEntry);
			addAlias(newEntry.getID(), parentEntry.getID());
		}

		this.compendium.put(entryKey, newEntry);
		try { // this method doesn't *actually* get used, but it can be used for overwriting entries, as a result, this part needs to be included
			if (Minecraft.getMinecraft().thePlayer != null && ExtendedProperties.For(Minecraft.getMinecraft().thePlayer) != null) {
				String state = "U";
				if (newEntry.isLocked)
					state += "L";
				if (newEntry.isNew)
					state += "N";
				ExtendedProperties.For(Minecraft.getMinecraft().thePlayer).setCompendiumEntry(entryKey, state);
			}
		} catch (Exception e) {e.printStackTrace();}

		LogHelper.debug("Successfully added compendium entry %s", entryKey);
	}

	public static CompendiumEntry getCompendiumEntry(Object entryItem){
		CompendiumEntry newEntry = null;

		if (entryItem instanceof Item){
			newEntry = new CompendiumEntryItem();
		}else if (entryItem instanceof Block){
			newEntry = new CompendiumEntryBlock();
		}else if (entryItem instanceof ISpellShape){
			newEntry = new CompendiumEntrySpellShape();
		}else if (entryItem instanceof ISpellComponent){
			newEntry = new CompendiumEntrySpellComponent();
		}else if (entryItem instanceof ISpellModifier){
			newEntry = new CompendiumEntrySpellModifier();
		}else if (entryItem instanceof Entity){
			newEntry = new CompendiumEntryMob();
		}
		return newEntry;
	}
}
