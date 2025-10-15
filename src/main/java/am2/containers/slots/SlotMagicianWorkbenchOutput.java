package am2.containers.slots;

import am2.containers.ContainerMagiciansWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

public class SlotMagicianWorkbenchOutput extends SlotCrafting{

	/**
	 * The craft matrix inventory linked to this result slot.
	 */
	private final IInventory craftMatrix;

	private final ContainerMagiciansWorkbench workbench;

	public SlotMagicianWorkbenchOutput(EntityPlayer player, IInventory craftMatrix, IInventory craftResult, ContainerMagiciansWorkbench workbench, int index, int x, int y){
		super(player,craftMatrix,craftResult,index,x,y);
		this.craftMatrix = craftMatrix;
		this.workbench = workbench;
	}

	@Override
	protected void onCrafting(ItemStack itemCrafted){
		ItemStack[] components = new ItemStack[this.craftMatrix.getSizeInventory()];
		for (int i = 0; i < components.length; ++i){
			if (this.craftMatrix.getStackInSlot(i) != null)
				components[i] = this.craftMatrix.getStackInSlot(i).copy();
			else
				components[i] = null;
		}
		this.workbench.getWorkbench().rememberRecipe(itemCrafted, components, this.craftMatrix.getSizeInventory() == 4);
		super.onCrafting(itemCrafted);
	}
}
