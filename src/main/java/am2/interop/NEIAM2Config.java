package am2.interop;


import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;


public class NEIAM2Config implements IConfigureNEI {


	@Override
	public String getName() {
		return "Ars Magica 2.5";
	}

	@Override
	public String getVersion() {
		return "1.4.1";
	}

	@Override
	public void loadConfig() {
		API.registerRecipeHandler(new RecipeHandlerEssence());
		API.registerUsageHandler(new RecipeHandlerEssence());
		API.registerRecipeHandler(new RecipeHandlerEnervator());
		API.registerUsageHandler(new RecipeHandlerEnervator());
	}

}
