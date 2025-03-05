package am2.api.power;


/**
 * @author Mithion
 *         This will eventually allow you to interface with the AM power network - currently it is not fully working.
 */
public interface IManaPower{

	/**
	 * Gets the total capacity of the power block
	 */
	int getCapacity();

	/**
	 * Get Stored Charge in block.
	 */

	int getCharge();
	/**
	 * Can this block provide power?
	 *
	 * @param type The power type we are checking for
	 */
	boolean canSendPower(PowerTypes type);

	/**
	 * Can this block relay power?
	 *
	 * @param type The type of power we are looking for
	 */
	boolean canRelayPower(PowerTypes type);

	/**
	 * Can this block request power?
	 */
	boolean canReceivePower();

	/**
	 * Is this block a Source block (obelisk, prism, dark aurem) ?
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
