package networkUtils;

import com.google.gson.Gson;

public class DraftMessage extends Message {

	int CharacterId;
	
	public DraftMessage(int char_id) {
		super();
		this.m_type = MessageType.DRAFT;
		this.CharacterId = char_id;
	}
	
	public int getId(){
		return this.CharacterId;
	}
	
	@Override
	public String toString() {
		return (new Gson()).toJson(this);
	}

}
