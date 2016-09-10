package networkUtils;

import com.google.gson.Gson;

public class ChallengeMessage extends Message {

	String from;
	String to;
	String opponent;
	Boolean isFirst;
	Boolean isSuccess;
	
	public ChallengeMessage(){
		super();
		this.m_type = MessageType.CHALLENGE;
		from = "";
		to = "";
		opponent = "";
		isFirst = false;
		isSuccess = false;
	}
	
	public ChallengeMessage(String from,String to,String opponent,Boolean isFirst,Boolean isSuccess) {
		super();
		this.from = from;
		this.to = to;
		this.m_type = MessageType.CHALLENGE;
		this.opponent = opponent;
		this.isFirst = isFirst;
		this.isSuccess = isSuccess;
	}
	
	public ChallengeMessage(String from,String to,String opponent,Boolean isFirst) {
		super();
		this.from = from;
		this.to = to;
		this.m_type = MessageType.CHALLENGE;
		this.opponent = opponent;
		this.isFirst = isFirst;
		this.isSuccess = true;
	}


	@Override
	public String toString() {
		return (new Gson()).toJson(this);
	}
	
	public Boolean getSuccess(){
		return this.isSuccess;
	}
	
	public String getOpponent(){
		return this.opponent;
	}

	public Boolean getIsFirst(){
		return this.isFirst;
	}
	
	public String getFrom(){
		return this.from;
	}
	
	public String getTo(){
		return this.to;
	}
	
}
