package am2.api.power;

import am2.api.math.AMVector3;
import net.minecraft.nbt.NBTTagCompound;

import java.util.LinkedList;

public interface IPowerPath extends IPowerNode{
	void clearNodePaths();

	void registerNodePath(PowerTypes type, LinkedList<AMVector3> path);

	void savePaths(NBTTagCompound compound);

	void readPaths(NBTTagCompound compound);


}
