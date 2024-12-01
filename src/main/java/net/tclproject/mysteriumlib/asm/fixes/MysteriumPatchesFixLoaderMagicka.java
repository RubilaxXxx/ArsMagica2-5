package net.tclproject.mysteriumlib.asm.fixes;

import am2.LogHelper;
import am2.preloader.BytecodeTransformers;
import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;
import net.tclproject.mysteriumlib.asm.common.FirstClassTransformer;

public class MysteriumPatchesFixLoaderMagicka extends CustomLoadingPlugin {

    public static boolean foundOptiFine = false;
    private static boolean confirmedOptiFine = false;

    // Turns on MysteriumASM Lib. You can do this in only one of your Fix Loaders.
    @Override
    public String[] getASMTransformerClass() {
        CustomLoadingPlugin.getMetaReader();
        return new String[]{
                FirstClassTransformer.class.getName(),
                BytecodeTransformers.class.getName(),
        };
    }

    @Override
    public void registerFixes() {
        //Registers the class where the methods with the @Fix annotation are
    	registerClassWithFixes("net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesMagicka");
    }

    public static boolean isOptiFinePresent(){
        if (!confirmedOptiFine && foundOptiFine){
            // Check presence of OptiFine core classes
            try{
                Class.forName("optifine.OptiFineForgeTweaker");
            }
            catch (ClassNotFoundException exception1){
                try{
                    Class.forName("optifine.OptiFineTweaker");
                }
                catch (ClassNotFoundException exception2){
                    foundOptiFine = false;
                }
            }
            if (foundOptiFine){
                LogHelper.info("Core: OptiFine presence has been confirmed.");
            } else {
                LogHelper.info("Core: OptiFine doesn't seem to be there actually.");
            }
            confirmedOptiFine = true;
        }
        return foundOptiFine;
    }
}
