package networkUtils;

import java.util.ArrayList;

import com.google.gson.Gson;

import flexjson.JSONSerializer;
import networkUtils.Message.MessageType;

public class LobbyMessage extends Message{
	
	ArrayList<String> usernames;
	
	public LobbyMessage(){
		super();
		m_type = MessageType.LOBBY;
		usernames = null;
	}

	public LobbyMessage(ArrayList<String> usernames) {
		super();
		this.m_type = MessageType.LOBBY;
		this.usernames = usernames;
	}
	
	@Override
	public String toString() {
		return (new Gson()).toJson(this);
	}

}
