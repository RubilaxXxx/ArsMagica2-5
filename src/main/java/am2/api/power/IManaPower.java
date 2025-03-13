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


	void setCharge(PowerTypes type, int amount);


	void setCharge(int amount);


	/**
	 * Can this block request power?
	 */
	boolean canReceivePower();

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
