package networkUtils;

import flexjson.JSONSerializer;
import networkUtils.Message.MessageType;

public class BackMessage extends Message {
	
	// I can imagine this would hold whatever information to handle disconnects
	// I am just having everything be a string right now but it would be
	// properly filled in with a set of attributes necessary to carry out the operation
	String disconnect;
	
	String from;
	String to;
	
	public BackMessage(){
		super();
		m_type = MessageType.INGAME;
		from = "";
		to = "";
		disconnect = "";
	}


	public BackMessage(String from,String to,String disconnect) {
		this.from = from;
		this.to = to;
		this.m_type = MessageType.BACK;
		this.disconnect = disconnect;
		// TODO Auto-generated constructor stub
	}
	
	public String getDest(){
		return to;
	}
	
	public String getFrom(){
		return from;
	}

	@Override
	public String toString() {
		String m = new JSONSerializer().deepSerialize(this);
		return m;
	}

}
