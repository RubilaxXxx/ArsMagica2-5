package am2.api.power;

import am2.LogHelper;
import am2.api.ArsMagicaApi;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public final class PowerTypes{

	public static final PowerTypes NONE = new PowerTypes(0, "None", "§f");
	public static final PowerTypes LIGHT = new PowerTypes(1, "Light", "§b");
	public static final PowerTypes NEUTRAL = new PowerTypes(2, "Neutral", "§1");
	public static final PowerTypes DARK = new PowerTypes(4, "Dark", "§4");
	public static final PowerTypes[] ALL = new PowerTypes[]{ LIGHT, NEUTRAL, DARK};
	private static final ArrayList<PowerTypes> allPowerTypes = new ArrayList<PowerTypes>(){{
		add(LIGHT);
		add(NEUTRAL);
		add(DARK);
	}};


	private int id;
	private String name;
	private String chatColor;

	private PowerTypes(int ID, String name, String chatColor){
		if ((ID & -ID) != ID){
			throw new InvalidParameterException(String.format("ID must be a bitflag that is a power of 2!  (You used %d)", ID));
		}
		this.id = ID;
		this.name = name;
		this.chatColor = chatColor;
	}

	public static void RegisterPowerType(int id, String name, String chatColor){
		if (getByID(id) != NONE){
			LogHelper.info("Attempted to register power type %s with ID of %d, but that ID is already taken!  The type was NOT registered!", name, id);
		}else{
			allPowerTypes.add(new PowerTypes(id, name, chatColor));
			LogHelper.info("Registered new power type %s with ID %d", name, id);
		}
	}

	public int ID(){
		return id;
	}

	public String name(){
		return name;
	}

	public String chatColor(){
		if (ArsMagicaApi.instance.getColourblindMode()){
			return "";
		}
		return chatColor;
	}

	public static PowerTypes[] all(){
		return allPowerTypes.toArray(new PowerTypes[allPowerTypes.size()]);
	}

	public static PowerTypes getByID(int id){
		for (PowerTypes type : all())
			if (type.ID() == id)
				return type;
		return NONE;
	}
}
