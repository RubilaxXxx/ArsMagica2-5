package am2.api.items.armor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IManaGoggle{
	boolean showingameHUD(World world, ItemStack stack, EntityPlayer player);

}
