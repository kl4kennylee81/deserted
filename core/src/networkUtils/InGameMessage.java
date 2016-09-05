package networkUtils;

import java.util.ArrayList;

import com.google.gson.Gson;

import edu.cornell.gdiac.ailab.CharacterActions;
import edu.cornell.gdiac.ailab.Characters;
import flexjson.JSONSerializer;
import networkUtils.Message.MessageType;

public class InGameMessage extends Message {
	
	// actually when we move this into the proper game repo 
	// the message will be of type of list of actionNodes
	
	CharacterActions characterActions;
	String from;
	String to;
	
	public InGameMessage(){
		super();
		m_type = MessageType.INGAME;
		from = "";
		to = "";
		characterActions = null;
	}

	public InGameMessage(String from, String to,CharacterActions characterActions) {
		this.m_type = MessageType.INGAME;
		this.from = from;
		this.to = to;
		this.characterActions = characterActions;;
	}
	
	public String getTo(){
		return to;
	}
	
	public String getFrom(){
		return from;
	}
	
	@Override
	public String toString() {
		return (new Gson()).toJson(this);
	}
	
	public CharacterActions getCharacterActions() {
		return this.characterActions;
	}


}
