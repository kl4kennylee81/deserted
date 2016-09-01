package networkUtils;

public class Message {
	
	enum MessageType {NORMAL};
	
	Integer fromId;
	Integer toId;
	
	MessageType m_type;
	String text;

	public Message(int from,int to,String m){
		m_type = MessageType.NORMAL;
		fromId = from;
		toId = to;
		text = m;
	}
	
	public String getMessage(){
		return text;
	}
	
	public int getDest(){
		return toId;
	}
	
	public int getFrom(){
		return fromId;
	}
}
