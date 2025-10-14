package am2.items;

import am2.containers.slots.SlotEssenceOnly;
import am2.containers.slots.SlotLock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerEssenceBag extends Container {

	private ItemStack bagStack;
	private InventoryEssenceBag essBagInventory;
	public int specialSlotIndex;

	private static final int mainInventoryStart = 12;

	public ContainerEssenceBag(InventoryPlayer inventoryplayer, ItemStack bagStack, InventoryEssenceBag inventoryBag) {
		this.essBagInventory = inventoryBag;
		this.bagStack = bagStack;

		int slotIndex = 0;

		// rune slots
		for (int x = 0; x < 6; ++x) {
			for (int y = 0; y < 2; ++y) {
				addSlotToContainer(new SlotEssenceOnly(essBagInventory, slotIndex++, 26 + (x * 18), 8 + (y * 18)));
			}
		}

		// display player inventory
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 9; k++) {
				addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 58 + i * 18));
			}
		}

		// display player action bar
		for (int j1 = 0; j1 < 9; j1++) {
			if (inventoryplayer.getStackInSlot(j1) == bagStack) {
				specialSlotIndex = j1 ;
				addSlotToContainer(new SlotLock(inventoryplayer, j1, 8 + j1 * 18, 116));
				continue;
			}
			addSlotToContainer(new Slot(inventoryplayer, j1, 8 + j1 * 18, 116));
		}

	}

	public ItemStack[] GetFullInventory() {
		ItemStack[] stack = new ItemStack[12];
		for (int i = 0; i < 12; ++i) {
			stack[i] = ((Slot) inventorySlots.get(i)).getStack();
		}
		return stack;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer) {
		World world = entityplayer.worldObj;

		if (!world.isRemote) {
			ItemStack essenceBagItemStack = bagStack;
			ItemEssenceBag bag = (ItemEssenceBag) essenceBagItemStack.getItem();
			ItemStack[] items = GetFullInventory();
			bag.UpdateStackTagCompound(essenceBagItemStack, items);
			if(entityplayer.getHeldItem() != null && entityplayer.getHeldItem().isItemEqual(bagStack)){
				entityplayer.setCurrentItemOrArmor(0,essenceBagItemStack);
			}
		}

		super.onContainerClosed(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return essBagInventory.isUseableByPlayer(entityplayer);
	}
	/**
	 * Override slotClick behavior to correctly handle quick-swap access security
	 */
	@Override
	public ItemStack slotClick(int slotId, int keyOrdinal, int clickType, EntityPlayer player) {
		if(clickType == 2 && keyOrdinal >= 0 && keyOrdinal < 9) {
			int hotbarSlotIndex = this.inventorySlots.size() - (9 - keyOrdinal);
			Slot hotbarTargetSlot = getSlot(hotbarSlotIndex);
			Slot hoverSlot = getSlot(slotId);
			if(hotbarTargetSlot instanceof SlotLock || hoverSlot instanceof SlotLock) {
				return null;
			}
		}
		return super.slotClick(slotId, keyOrdinal, clickType, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int index) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < mainInventoryStart) {
				if (!this.mergeItemStack(itemstack1, mainInventoryStart, this.inventorySlots.size(), true)) {
					return null;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, mainInventoryStart, false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
}
