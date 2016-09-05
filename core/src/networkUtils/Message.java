package networkUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import edu.cornell.gdiac.ailab.ActionNode;
import edu.cornell.gdiac.ailab.GameActionNode;
import edu.cornell.gdiac.ailab.CharacterActions.MessageActionNode;

public class Message {
	
	public enum MessageType {
		@SerializedName("0")
		NORMAL (0),
		@SerializedName("1")
		USERNAME (1),
		@SerializedName("2")
		LOBBY (2),
		@SerializedName("3")
		CHALLENGE (3),
		@SerializedName("4")
		INGAME (4),
		@SerializedName("5")
		BACK (5),
		@SerializedName("6")
		DRAFT (6);
		
	    private final int value;
	    public int getValue() {
	        return value;
	    }

	    private MessageType(int value) {
	        this.value = value;
	    }
	
	
	};
	
	MessageType m_type;
	
	public Message(){
		m_type = MessageType.NORMAL;
	}
	
	public MessageType getType(){
		return this.m_type;
	}
	
	public static String byteBufferToString(ByteBuffer bb){
		  bb.flip();
	      Charset cs = Charset.forName("UTF-8");
	      int limits = bb.limit();
	      byte bytes[] = new byte[limits];
	      bb.get(bytes, 0, limits);
	      String msg = new String(bytes, cs);
	      System.out.println(msg);
	      bb.clear();
	      return msg;
	}
	
	public static Message byteBufferToMsg(ByteBuffer bb){
		String s = byteBufferToString(bb);
		Message msg = jsonToMsg(s);
		return msg;
	}
	
	public ByteBuffer msgToByteBuffer(ByteBuffer bb){
		return strToByteBuffer(bb,this.toString());
	}
	
	public ByteBuffer msgToByteBuffer(){
		return ByteBuffer.wrap(this.toString().getBytes());
	}
	
	public static ByteBuffer strToByteBuffer(ByteBuffer bb,String msg){
	    Charset cs = Charset.forName("UTF-8");
	    byte[] data = msg.toString().getBytes(cs);
	    bb.clear();
	    bb.put(data);
	    bb.flip();
	    return bb;
	}
	
	public static ByteBuffer strToByteBuffer(String msg){
	    Charset cs = Charset.forName("UTF-8");
	    ByteBuffer bb = ByteBuffer.wrap(msg.getBytes(cs));
	    return bb;
	}
	
	public static Message jsonToMsg(String s){
		String processedS = s.trim();
		System.out.println(processedS);
		RuntimeTypeAdapterFactory<Message> messageAdapter = RuntimeTypeAdapterFactory.of(Message.class, new MessageTypePredicate())
		        .registerSubtype(NormalMessage.class)
		        .registerSubtype(UsernameMessage.class)
		        .registerSubtype(LobbyMessage.class)
		        .registerSubtype(ChallengeMessage.class)
		        .registerSubtype(InGameMessage.class)
		        .registerSubtype(BackMessage.class)
		        .registerSubtype(DraftMessage.class);
		
		RuntimeTypeAdapterFactory<ActionNode> anAdapter = RuntimeTypeAdapterFactory.of(ActionNode.class, new ActionNodeTypePredicate())
		        .registerSubtype(MessageActionNode.class)
				.registerSubtype(GameActionNode.class);

		Gson gson = new GsonBuilder()
		        .enableComplexMapKeySerialization()
		        .registerTypeAdapterFactory(messageAdapter)
		        .registerTypeAdapterFactory(anAdapter).create();
		
		Message m = gson.fromJson(processedS,Message.class);
		return m;
	}
}
