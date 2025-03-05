package am2.api.power;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IBindable{

	boolean bindTo(World world, int x, int y, int z, EntityPlayer player);

	boolean unbind(World world, int x, int y, int z);
}
