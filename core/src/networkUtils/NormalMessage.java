package networkUtils;

import flexjson.JSONSerializer;
import networkUtils.Message.MessageType;

public class NormalMessage extends Message{

	String text;
	String from;
	String to;
	
	public NormalMessage(){
		super();
		m_type = MessageType.NORMAL;
		text = "";
		from = "";
		to = "";
	}
	
	public NormalMessage(String from,String to,String text) {
		super();
		this.m_type = MessageType.NORMAL;
		this.text = text;
		this.from = from;
		this.to = to;
	}
	
	public NormalMessage(String msg) {
		super();
		this.m_type = MessageType.NORMAL;
		this.text = msg;
		this.from = "";
		this.to = "";
	}
	
	public String getTo(){
		return this.to;
	}
	
	public String getFrom(){
		return this.from;
	}
	
	@Override
	public String toString() {
		String m = new JSONSerializer().deepSerialize(this);
		return m;
	}


}
