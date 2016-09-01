package desertedServer;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import networkUtils.Connection;
import networkUtils.Message;

public class GameState {
	
	HashMap<Integer,Queue<Message>> connectionToQ;

	public GameState(Connection p1,Connection p2) {
		connectionToQ = new HashMap<Integer,Queue<Message>>();
		connectionToQ.put(p1.getId(), new LinkedBlockingQueue<Message>());
		connectionToQ.put(p2.getId(), new LinkedBlockingQueue<Message>());
	}
	
	public boolean write(Message m){
		Queue<Message> q = connectionToQ.get(m.getDest());
		return q.offer(m);
	}
	
	public Message read(int id){
		Queue<Message> q = connectionToQ.get(id);
		return q.poll();
	}

}
