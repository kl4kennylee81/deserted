package networkUtils;

import flexjson.JSONSerializer;
import networkUtils.Message.MessageType;

public class InGameMessage extends Message {
	
	// actually when we move this into the proper game repo 
	// the message will be of type of list of actionNodes
	String actionNodeList;
	
	String from;
	String to;
	
	public InGameMessage(){
		super();
		m_type = MessageType.INGAME;
		from = "";
		to = "";
		actionNodeList = "";
	}

	public InGameMessage(String from, String to,String actionNodeList) {
		this.m_type = MessageType.INGAME;
		this.from = from;
		this.to = to;
		this.actionNodeList = actionNodeList;
	}
	
	public String getTo(){
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
