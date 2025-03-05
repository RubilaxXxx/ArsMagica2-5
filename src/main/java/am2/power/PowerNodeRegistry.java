package am2.power;

import am2.LogHelper;
import am2.api.math.AMVector3;
import am2.api.power.IManaPower;
import am2.api.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

public class PowerNodeRegistry{

	private static final HashMap<Integer, PowerNodeRegistry> serverDimensionPowerManagers = new HashMap<Integer, PowerNodeRegistry>();
	private static final HashMap<Integer, PowerNodeRegistry> clientDimensionPowerManagers = new HashMap<Integer, PowerNodeRegistry>();

	static final int POWER_SEARCH_RADIUS = 10; //The literal power search radius
	static final int POWER_SEARCH_RADIUS_SQ = 100; //The power search radius squared.  This is used for faster vector math.
	static final int MAX_POWER_SEARCH_RADIUS = 10000; //The maximum power search radius (squared)
	private HashMap<World, List<TileEntity>> powerNodes = new HashMap<>();
	private HashMap<IManaPower, PowerNodeEntry> nodeData = new HashMap<>();

	public static PowerNodeRegistry instance = new PowerNodeRegistry();

	public void add(IManaPower node){
		TileEntity Tnode = (TileEntity) node;
		World Tworld = Tnode.getWorldObj();
		if(!powerNodes.containsKey(Tworld)){
			powerNodes.put(Tworld, new ArrayList<>());
		}
		List<TileEntity> nodes = powerNodes.get(Tworld);

		if(!nodes.contains(Tnode)){
			nodes.add(Tnode);
		}
	}

	//remove node from Nodes list & nodeData.
	public void remove(IManaPower node){
		TileEntity Tnode = (TileEntity) node;
		World Tworld = Tnode.getWorldObj();
		if(!powerNodes.containsKey(Tworld))
			return;
		List<TileEntity> nodes = powerNodes.get(Tworld);
		for(TileEntity tile : nodes){
			if(tile == Tnode){
				nodes.remove(tile);
				break;
			}
		}
		for (IManaPower nodep : nodeData.keySet()){
			if(nodep == node){
				nodeData.remove(nodep);
				break;
			}
		}
	}
	public void clear(){
		powerNodes.clear();
		nodeData.clear();
	}




	/**
	* request power to a specific destination
	* @param destination the block receiving mana
	 * @param type the type of power requested
	 * @param amount amount of power requested.
	 * @return float return 0 if destination has no data otherwise return amount.
	 */
	public float requestPower(IManaPower destination, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(destination);

		if (data == null){
			return 0;
		}

		float requested = data.requestPower(((TileEntity)destination).getWorldObj(), type, amount, destination.getCapacity());

		return requested;
	}
	/**
	 * comsume power from a block.
	 * @param consumer the block consuming mana.
	 * @param type the type of power requested.
	 * @param amount amount of power requested.
	 * @return float return the amount of power consumed.
	 */
	public float consumePower(IManaPower consumer, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(consumer);

		if (data == null){
			return 0;
		}

		float availablePower = data.getPower(type);
		if (availablePower < amount)
			amount = availablePower;
		data.setPower(type, availablePower - amount);
		return amount;
	}
	/**
	 * add  power to a block.
	 * @param destination the block getting mana.
	 * @param type the type of power.
	 * @param amount amount of power to insert.
	 * @return float return the amount of power inserted.
	 */
	public float insertPower(IManaPower destination, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(destination);
		if (data == null){
			return 0;
		}
		float curPower = data.getPower(type);
		if (curPower + amount > destination.getCapacity())
			amount = destination.getCapacity() - curPower;
		data.setPower(type, curPower + amount);

		return amount;
	}

	public void setPower(IManaPower destination, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(destination);

		if (data == null){
			return;
		}

		if (amount > destination.getCapacity())
			amount = destination.getCapacity();
		data.setPower(type, amount);
	}
	/**
	 * check if specified node has  a specific amount of power for a type.
	 * @param node the node who's getting checked.
	 * @param amount the amount of power to check.
	 * @param type the type of power to check.
	 * @return true if node has  >+ amount of power for type, false otherwise.
	 */
	public boolean checkPower(IManaPower node, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return false;
		}

		return data.getPower(type) >= amount;
	}
	/**
	 * check if specified node has  a specific amount of power for any type .
	 * @param node the node who's getting checked.
	 * @param amount the amount of power to check.
	 * @return true if node has  >+ amount of power, false otherwise.
	 */
	public boolean checkPower(IManaPower node, float amount){
		for (PowerTypes type : PowerTypes.all())
			if (checkPower(node, type, amount))
				return true;
		return false;
	}
	/**
	 * check if specified node has  any type of power.
	 * @param node the node who's getting checked.
	 * @return true if node has power, false otherwise.
	 */
	public boolean checkPower(IManaPower node){
		return getHighestPower(node) > 0;
	}
	/**
	 * get current stored power in a node.
	 * @param node the node who's getting checked.
	 * @param type type of power.
	 * @return float amount of power stored.
	 */
	public float getPower(IManaPower node, PowerTypes type){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return 0;
		}

		return data.getPower(type);
	}
	/**
	 * get current stored power in a node for highest powerType.
	 * @param node the node who's getting checked.
	 * @return float amount of power stored.
	 */
	public float getHighestPower(IManaPower node){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return 0;
		}

		return data.getHighestPower();
	}
	/**
	 * get PowerType with highest stored power.
	 * @param node the node who's getting checked.
	 * @return the Powertype with the highest amount.
	 */
	public PowerTypes getHighestPowerType(IManaPower node){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return PowerTypes.NONE;
		}

		return data.getHighestPowerType();
	}

	public PowerNodeEntry getPowerNodeData(IManaPower node){
		if(nodeData.get(node) == null){
			nodeData.put(node, new PowerNodeEntry());
		}
		return nodeData.get(node);
	}
	public boolean IsNodeInit(TileEntity tile){
		World world = tile.getWorldObj();
		if(powerNodes.get(world) == null)
			return false;
		else{
			for(TileEntity tiles : powerNodes.get(world))
				if(tiles == tile)
					return true;
		}
		return false;
	}

	/**
	 * Returns all power nodes within POWER_SEARCH_RADIUS
	 *
	 * @param world The world instance.
	 * @param chunk the chunk coord of the block.
	 * @return A list of IManaPower.
	 */
	public List<IManaPower> getAllNearbyNodes(World world, ChunkCoordinates chunk){
		//get the list of chunks we'll need to search in
		List<IManaPower> nodesnearby = new ArrayList<>(Collections.emptyList());
		if(!powerNodes.containsKey(world))
			return null ;
		List<TileEntity> te = powerNodes.get(world);
		AMVector3 vectChunk = new AMVector3(chunk.posX,chunk.posY,chunk.posZ);
		for(TileEntity tile : te){
			AMVector3 vect1 = new AMVector3(tile);
			if(tile.isInvalid())
				continue;
			if(vect1.distanceSqTo(vectChunk) > POWER_SEARCH_RADIUS_SQ)
				continue;
			nodesnearby.add((IManaPower)tile);

		}
		return nodesnearby;
	}
	/*
	private HashMap<ChunkCoordIntPair[], PowerNodeEntry> getChunkpairFromNodelist(HashMap<AMVector3, PowerNodeEntry> hashmap){
		HashMap<ChunkCoordIntPair[], PowerNodeEntry> values = new HashMap<>();
		for(AMVector3 vector : hashmap.keySet()){
			ChunkCoordIntPair[] chunk =  getSearchChunks(vector);
			values.put(chunk, hashmap.get(vector));
		}
		return values;
	}
	private ChunkCoordIntPair[] getSearchChunks(AMVector3 location){
		//Calculate the chunk X/Z location of the search center
		int chunkX = (int)location.x >> 4;
		int chunkZ = (int)location.z >> 4;

		ArrayList<ChunkCoordIntPair> searchChunks = new ArrayList<ChunkCoordIntPair>();
		//always search the chunk you're already in!
		searchChunks.add(new ChunkCoordIntPair(chunkX, chunkZ));

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				//don't include the home chunk, this has already been added.  We're only concerned with neighboring chunks.
				if (i == 0 && j == 0) continue;
				//create the new CCIP at the offset location
				ChunkCoordIntPair newPair = new ChunkCoordIntPair(chunkX + i, chunkZ + j);
				//offset the x/z locations by POWER_SEARCH_RADIUS * i/j respectively and calculate the chunk that would fall in.
				//If it matches the new chunk coordinates, we add that chunk to the list of chunks to search.
				if (((int)location.x + (POWER_SEARCH_RADIUS * i)) >> 4 == newPair.chunkXPos && ((int)location.z + (POWER_SEARCH_RADIUS * j)) >> 4 == newPair.chunkZPos){
					searchChunks.add(newPair);
				}
			}
		}

		return searchChunks.toArray(new ChunkCoordIntPair[searchChunks.size()]);
	}

	private ChunkCoordIntPair getChunkFromNode(IManaPower node){
		TileEntity te = (TileEntity)node;
		if (te.getWorldObj() == null)
			return null;
		if (!te.getWorldObj().checkChunksExist(te.xCoord, 0, te.zCoord, te.xCoord, te.getWorldObj().getActualHeight(), te.zCoord))
			return new ChunkCoordIntPair(te.xCoord >> 4, te.zCoord >> 4);
		return te.getWorldObj().getChunkFromBlockCoords(te.xCoord, te.zCoord).getChunkCoordIntPair();
	}

	public NBTTagCompound getDataCompoundForNode(IManaPower node){
		PowerNodeEntry pnd = getPowerNodeData(node);
		if (pnd == null)
			return null;
		return pnd.saveToNBT();
	}

	public void setDataCompoundForNode(IManaPower node, NBTTagCompound compound){
		PowerNodeEntry pnd = getPowerNodeData(node);
		if (pnd == null)
			return;
		pnd.readFromNBT((NBTTagCompound)compound.copy());
	}

	public PowerNodeEntry parseFromNBT(NBTTagCompound compound){
		PowerNodeEntry pnd = new PowerNodeEntry();
		pnd.readFromNBT((NBTTagCompound)compound.copy());
		return pnd;
	}


	  Attempts to pair the source and destination nodes by either a direct link or by going through conduits.

	  @param powerSource The power source
	  @param destination The destination point
	  @return A localized message to return to the entity attempting to pair the nodes, either of success or why it failed.


	public String tryPairNodes(IManaPower powerSource, IManaPower destination){
		//some simple validation

		if (powerSource == destination){
			return StatCollector.translateToLocal("am2.tooltip.nodePairToSelf");
		}

		//Can the power source provide any of the valid power types for the destination?
		ArrayList<PowerTypes> typesProvided = new ArrayList<PowerTypes>();
		for (PowerTypes type : destination.getValidPowerTypes()){
			if (powerSource.canSendPower(type)){
				typesProvided.add(type);
			}
		}
		if (typesProvided.isEmpty()){
			//no valid power types can be provided
			return StatCollector.translateToLocal("am2.tooltip.noSupportedPowertypes");
		}

		//set up vectors and calculate distance for pathing purposes
		AMVector3 sourceLocation = new AMVector3((TileEntity)powerSource);
		AMVector3 destLocation = new AMVector3((TileEntity)destination);
		double rawDist = sourceLocation.distanceSqTo(destLocation);

		if (rawDist > MAX_POWER_SEARCH_RADIUS){
			return StatCollector.translateToLocal("am2.tooltip.nodesTooFar");
		}

		//construct a list of all valid power types common between the source and destination
		int successes = 0;

		for (PowerTypes type : typesProvided){
			LinkedList<AMVector3> powerPath = new LinkedList<AMVector3>();
			PowerNodePathfinder pathfinder = new PowerNodePathfinder(((TileEntity)powerSource).getWorldObj(), sourceLocation, destLocation, type);
			List<AMVector3> path = pathfinder.compute(sourceLocation);
			if (path == null)
				continue;
			for (AMVector3 vec : path){
				powerPath.addFirst(vec);
			}
			successes++;
			getPowerNodeData(destination).registerNodePath(type, powerPath);
		}

		//are the nodes too far apart?
		if (successes == 0){
			return StatCollector.translateToLocal("am2.tooltip.noPathFound");
		}

		if (successes == typesProvided.size())
			return StatCollector.translateToLocal("am2.tooltip.success");
		return StatCollector.translateToLocal("am2.tooltip.partialSuccess");
	}

	  Attempts to disconnect all sources powering the passed-in block

	public void tryDisconnectAllNodes(IManaPower node){
		getPowerNodeData(node).clearNodePaths();
	}
*/
}
