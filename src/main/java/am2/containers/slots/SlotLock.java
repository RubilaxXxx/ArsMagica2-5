package am2.containers.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotLock extends Slot{

	public SlotLock(IInventory inv, int slotIndex, int x, int y) {
		super(inv, slotIndex, x, y);
	}

	@Override
	public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack p_82870_2_) {
	}
	@Override
	public boolean isItemValid(ItemStack p_75214_1_) {
		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer var1) {
		return false;
	}

	@Override
	public boolean getHasStack() {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int i) {
		return null;
	}

}
