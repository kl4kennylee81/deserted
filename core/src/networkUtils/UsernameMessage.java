package networkUtils;

import flexjson.JSONSerializer;
import networkUtils.Message.MessageType;

public class UsernameMessage extends Message {

	String username;
	
	public UsernameMessage(){
		super();
		m_type = MessageType.USERNAME;
		username = "";
	}
	
	
	public UsernameMessage(String username) {
		super();
		this.m_type = MessageType.USERNAME;
		this.username = username;
	}

	public String getUsername(){
		return this.username;
	}
	
	@Override
	public String toString() {
		String m = new JSONSerializer().deepSerialize(this);
		return m;
	}

	
}