package am2.api.power;

import net.minecraft.tileentity.TileEntity;

/**
 * @author Mithion
 *         This will eventually allow you to interface with the AM power network - currently it is not fully working.
 */
public interface IPowerNode<T extends TileEntity>{

	/**
	 * Gets the current capacity in the power block
	 */
	float getCapacity();

	/**
	 * Can this block provide power?
	 *
	 * @param type The power type we are checking for
	 */
	boolean canProvidePower(PowerTypes type);

	/**
	 * Can this block relay power?
	 *
	 * @param type The type of power we are looking for
	 */
	boolean canRelayPower(PowerTypes type);

	/**
	 * Can this block request power?
	 */
	boolean canRequestPower();

	/**
	 * Is this block a nexus?
	 */
	boolean isSource();

	/**
	 * How fast does this block charge?
	 */
	int getChargeRate();

	/**
	 * Gets the current power type of the block.
	 */
	PowerTypes[] getValidPowerTypes();

	/**
	 * Offset of any particle effects to the origin of the block
	 *
	 * @param axis The axis in question.  0=x, 1=y, 2=z
	 */
	float particleOffset(int axis);

}
