package am2.power;

import am2.api.math.AMVector3;
import am2.api.power.IManaPower;
import am2.api.power.PowerTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

//public class PowerNodePathfinder extends AStar<AMVector3>{
//
//	private  World world;
//	private AMVector3 start;
//	private AMVector3 end;
//	private PowerTypes powerType;

//	PowerNodePathfinder(World world, AMVector3 start, AMVector3 end, PowerTypes type){
//		this.world = world;
//		this.start = start;
//		this.end = end;
//		this.powerType = type;
//	}
//
//	private IManaPower getPowerNode(World world, AMVector3 location){
//		if (world.checkChunksExist((int)location.x, (int)location.y, (int)location.z, (int)location.x, (int)location.y, (int)location.z)){
//			Chunk chunk = world.getChunkFromBlockCoords((int)location.x, (int)location.z);
//			if (chunk.isChunkLoaded){
//				TileEntity te = world.getTileEntity((int)location.x, (int)location.y, (int)location.z);
//				if (te instanceof IManaPower)
//					return (IManaPower)te;
//			}
//		}
//		return null;
//	}

//	@Override
//	protected boolean isGoal(AMVector3 node){
//		return node.equals(end);
//	}

//	@Override
//	protected Double g(AMVector3 from, AMVector3 to){
//		return from.distanceSqTo(to);
//	}

//	@Override
//	protected Double h(AMVector3 from, AMVector3 to){
//		return from.distanceSqTo(to);
//	}

//	@Override
//	protected List<AMVector3> generateSuccessors(AMVector3 node){
//		IManaPower powerNode = getPowerNode(world, node);
//		if (powerNode == null)
//			return new ArrayList<AMVector3>();
//		ChunkCoordinates chunk = new ChunkCoordinates((int)node.x, (int)node.y, (int)node.z);
//		List<IManaPower> candidates = PowerNodeRegistry.instance.getAllNearbyNodes(world, chunk);
//
//		ArrayList<AMVector3> prunedCandidates = new ArrayList<AMVector3>();
//		for (IManaPower candidate : candidates){
//			if (verifyCandidate(candidate)){
//				prunedCandidates.add(new AMVector3((TileEntity)candidate));
//			}
//		}
//
//		return prunedCandidates;
//	}
//
//	private boolean verifyCandidate(IManaPower powerNode){
//		if (new AMVector3((TileEntity)powerNode).equals(end)){
//			for (PowerTypes type : powerNode.getValidPowerTypes())
//				if (type == powerType)
//					return true;
//		}
//		return false;
//	}
//}
