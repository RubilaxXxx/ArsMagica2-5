package am2.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class CompendiumProgressMessage implements IMessage{

	private boolean complete;
	private int entityid;
	private int x;
	private int y;
	private int z;
	public CompendiumProgressMessage(){}
	public CompendiumProgressMessage(boolean b, int entityid, int x1, int y1, int z1){
		this.complete = b;
		this.entityid = entityid;
		this.x = x1;
		this.y = y1;
		this.z = z1;
	}

	public boolean getComplete(){
		return complete;
	}
	public int getZ(){
		return z;
	}

	public int getY(){
		return y;
	}

	public int getX(){
		return x;
	}

	public int getEntityid(){
		return entityid;
	}

	/**
	 * Convert from the supplied buffer into your specific message type
	 *
	 * @param buf
	 */
	@Override
	public void fromBytes(ByteBuf buf){
		complete =  buf.readBoolean();
		entityid = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	/**
	 * Deconstruct your message into the supplied byte buffer
	 *
	 * @param buf
	 */
	@Override
	public void toBytes(ByteBuf buf){
		buf.writeBoolean(complete);
		buf.writeInt(entityid);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);

	}


}
