package am2;

import am2.blocks.BlocksCommonProxy;
import am2.items.ItemsCommonProxy;
import am2.network.CompendiumProgressMessage;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemFrameWatcher{

	private final HashMap<EntityItemFrameComparator, Integer> watchedFrames;
	private final ArrayList<EntityItemFrameComparator> queuedAddFrames;
	private final ArrayList<EntityItemFrameComparator> queuedRemoveFrames;

	private static final int processTime = 800;

	public ItemFrameWatcher(){
		watchedFrames = new HashMap<>();
		queuedAddFrames = new ArrayList<>();
		queuedRemoveFrames = new ArrayList<>();
	}

	public void checkWatchedFrames(){
		ArrayList<EntityItemFrameComparator> toRemove = new ArrayList<>();

		updateQueuedChanges();

		for (EntityItemFrameComparator frameComp : watchedFrames.keySet()){

			Integer time = watchedFrames.get(frameComp);
			if (time == null) time = 0;

			if (frameComp == null || frameComp.frame == null || frameComp.frame.worldObj == null)
				continue;

			if (!frameComp.frame.worldObj.isRemote || time >= processTime)
				toRemove.add(frameComp);

			if (frameIsValid(frameComp.frame)){
				if (!checkFrameRadius(frameComp)){
					toRemove.remove(frameComp);
				}
			}else{
				time++;
				watchedFrames.put(frameComp, time);
			}
		}

		for (EntityItemFrameComparator frame : toRemove){
			stopWatchingFrame(frame.frame);
		}
	}

	private boolean checkFrameRadius(EntityItemFrameComparator frameComp){

		int radius = 2;

		boolean shouldRemove = true;

		EntityItemFrame frame = frameComp.frame;

		List<Block> targetBlock = new ArrayList<>();

		if (AMCore.config.isAlternativeStart()){
			targetBlock.add(BlocksCommonProxy.witchwoodLeaves);
			targetBlock.add(BlocksCommonProxy.witchwoodLog);
		} else {
			targetBlock.add(BlocksCommonProxy.liquidEssence);
		}

		for (int i = -radius; i <= radius; ++i){
			for (int j = -radius; j <= radius; ++j){
				for (int k = -radius; k <= radius; ++k){

					if (targetBlock.contains(frame.worldObj.getBlock((int)frame.posX + i, (int)frame.posY + j, (int)frame.posZ + k))){

						Integer time = watchedFrames.get(frameComp);
						if (time == null){
							time = 0;
						}
						time++;

						watchedFrames.put(frameComp, time);

						if (time >= processTime){
							if (!frame.worldObj.isRemote){
								frame.setDisplayedItem(new ItemStack(ItemsCommonProxy.arcaneCompendium));
								return true;
							}
						}else{
							shouldRemove = false;
							AMCore.ITEMFRAMENETWORK.sendToAllAround(new CompendiumProgressMessage(false, frame.getEntityId(), (int)frame.posX + i, (int)frame.posY + j, (int)frame.posZ + k), new NetworkRegistry.TargetPoint(frame.dimension, (int)frame.posX + i, (int)frame.posY + j, (int)frame.posZ + k, 32));
						}
					}

				}
			}
		}

		return shouldRemove;
	}

	private boolean frameIsValid(EntityItemFrame frame){
		return frame != null && !frame.isDead && frame.getDisplayedItem() != null && frame.getDisplayedItem().getItem() instanceof ItemBook;
	}

	private void updateQueuedChanges(){

		//safe copy to avoid CME
		EntityItemFrameComparator[] toAdd = queuedAddFrames.toArray(new EntityItemFrameComparator[queuedAddFrames.size()]);
		queuedAddFrames.clear();

		for (EntityItemFrameComparator comp : toAdd){
			if (comp.frame != null && (comp.frame.getDisplayedItem() == null || comp.frame.getDisplayedItem().getItem() != ItemsCommonProxy.arcaneCompendium))
				watchedFrames.put(comp, 0);
		}

		//safe copy to avoid CME, again with queued removes
		EntityItemFrameComparator[] toRemove = queuedRemoveFrames.toArray(new EntityItemFrameComparator[queuedRemoveFrames.size()]);
		queuedRemoveFrames.clear();

		for (EntityItemFrameComparator comp : toRemove){
			Integer time = watchedFrames.get(comp);
			if (time != null && time >= processTime &&
					comp.frame != null && !comp.frame.isDead && !comp.frame.worldObj.isRemote &&
					(comp.frame.getDisplayedItem() != null &&
							(comp.frame.getDisplayedItem().getItem() == Items.book || comp.frame.getDisplayedItem().getItem() == ItemsCommonProxy.arcaneCompendium))){
				AMCore.ITEMFRAMENETWORK.sendToAllAround(new CompendiumProgressMessage(true, comp.frame.getEntityId(), (int)comp.frame.posX , (int)comp.frame.posY , (int)comp.frame.posZ), new NetworkRegistry.TargetPoint(comp.frame.dimension, comp.frame.posX, comp.frame.posY, comp.frame.posZ, 32));
			}
			watchedFrames.remove(comp);
		}
	}

	public void startWatchingFrame(EntityItemFrame frame){
		queuedAddFrames.add(new EntityItemFrameComparator(frame));
	}

	public void stopWatchingFrame(EntityItemFrame frame){
		queuedRemoveFrames.add(new EntityItemFrameComparator(frame));
	}



	private class EntityItemFrameComparator{
		private final EntityItemFrame frame;

		public EntityItemFrameComparator(EntityItemFrame frame){
			this.frame = frame;
		}

		@Override
		public boolean equals(Object obj){
			if (frame == null) return false;
			if (obj instanceof EntityItemFrame){
				return ((EntityItemFrame)obj).getEntityId() == frame.getEntityId() && ((EntityItemFrame)obj).worldObj.isRemote == frame.worldObj.isRemote;
			}
			if (obj instanceof EntityItemFrameComparator){
				return ((EntityItemFrameComparator)obj).frame.getEntityId() == frame.getEntityId() && ((EntityItemFrameComparator)obj).frame.worldObj.isRemote == frame.worldObj.isRemote;
			}
			return false;
		}

		@Override
		public int hashCode(){
			if (frame == null || frame.worldObj == null) return 0;
			return frame.getEntityId() + (frame.worldObj.isRemote ? 1 : 2);
		}
	}
}
