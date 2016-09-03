package networkUtils;

import flexjson.JSONSerializer;
import flexjson.locators.TypeLocator;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import flexjson.JSONDeserializer;

public abstract class Message {
	
	public enum MessageType {NORMAL,USERNAME,LOBBY,CHALLENGE,INGAME,BACK};
	
	MessageType m_type;
	
	public Message(){
		m_type = MessageType.NORMAL;
	}
	
	public MessageType getType(){
		return this.m_type;
	}
	
	public static String byteBufferToString(ByteBuffer bb){
	      Charset cs = Charset.forName("UTF-8");
	      int limits = bb.limit();
	      byte bytes[] = new byte[limits];
	      bb.rewind();
	      bb.get(bytes, 0, limits);
	      bb.rewind();
	      String msg = new String(bytes, cs);
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
		bb.clear();
	    Charset cs = Charset.forName("UTF-8");
	    byte[] data = msg.toString().getBytes(cs);
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
		Message m = (new JSONDeserializer<Message>()).deserialize(s);
		System.out.println(m.toString());
		return m;
	}
}
