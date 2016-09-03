package desertedServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.InGameMessage;
import networkUtils.LobbyMessage;
import networkUtils.Message;
import networkUtils.UsernameMessage;

public class DesertedServer {
	
	AsynchronousServerSocketChannel server;
	ConcurrentHashMap<AsynchronousSocketChannel,AsynchronousSocketChannel> p1vp2;
	ConcurrentHashMap<String,AsynchronousSocketChannel> userNameToConnect;
	Queue<AsynchronousSocketChannel> waitingQueue;
	
	public DesertedServer(){
		waitingQueue = new LinkedBlockingQueue<AsynchronousSocketChannel>();
		p1vp2 = new ConcurrentHashMap<AsynchronousSocketChannel,AsynchronousSocketChannel>();
		userNameToConnect = new ConcurrentHashMap<String,AsynchronousSocketChannel>();
	}
	
	public void addUsername(String username, AsynchronousSocketChannel socket){
		userNameToConnect.put(username, socket);
	}
	
	public DesertedServer(AsynchronousServerSocketChannel serverSock){
		this();
		this.server = serverSock;
	}
	
	public AsynchronousSocketChannel getPlayersOppSock(String playerUsername){
		AsynchronousSocketChannel sockPlayer = userNameToConnect.get(playerUsername);
		AsynchronousSocketChannel sockOpp = p1vp2.get(sockPlayer);
		return sockOpp;
	}
	
	public String getPlayersOppName(String playerUsername){
		AsynchronousSocketChannel sockPlayer = userNameToConnect.get(playerUsername);
		AsynchronousSocketChannel sockOpp = p1vp2.get(sockPlayer);
		String oppName = this.getUserFromSocket(sockOpp);
		return oppName;
	}
	
	public AsynchronousSocketChannel getSockFromUser(String username){
		return userNameToConnect.get(username);
	}
	
	public String getUserFromSocket(AsynchronousSocketChannel user){
		String name = null;
		Set<Entry<String,AsynchronousSocketChannel>> setEntries = userNameToConnect.entrySet();
		for (Entry<String,AsynchronousSocketChannel> entries : setEntries){
			if (entries.getValue().equals(user)){
				name = entries.getKey();
				break;
			}
		}
		return name;
	}
	
	public ArrayList<String> getUsers(){
		ArrayList<String> users = new ArrayList<String>();
		Enumeration<String> usernames = userNameToConnect.keys();
		while(usernames.hasMoreElements()){
			String user = usernames.nextElement();
			users.add(user);
		}
		return users;
	}
	
	public String getChallenger(AsynchronousSocketChannel user) throws InterruptedException{
		
		AsynchronousSocketChannel opp = waitingQueue.poll();
		synchronized(waitingQueue){
			if (opp != null) {
				System.out.println("did we pop something\n");
				p1vp2.put(user, opp);
				p1vp2.put(opp, user);
				waitingQueue.notifyAll();
				return getUserFromSocket(opp);
			}
			else {
				boolean addedSelf = false;
				while (!p1vp2.containsKey(user)){
					System.out.println("are we waiting\n");
					if (!addedSelf){
						waitingQueue.add(user);
					}
					waitingQueue.wait();
				}
				opp = p1vp2.get(user);
				return getUserFromSocket(opp);
			}
		}
	}
	
	public AsynchronousServerSocketChannel getServerSock(){
		return this.server;
	}
	
  public static void main(String[] args) throws Exception {
	// default host and port
	String host = "localhost";
	int port = 8989;
	if (args.length > 2){
		host = args[0];
		port = Integer.parseInt(args[1]);
	}
	  
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel
        .open();
    InetSocketAddress sAddr = new InetSocketAddress(host, port);
    server.bind(sAddr);
    System.out.format("Server is listening at %s%n", sAddr);
    
    DesertedServer dserver = new DesertedServer(server);
    Attachment attach = new Attachment();
    attach.server = dserver;
    server.accept(attach, new ConnectionHandler());
    Thread.currentThread().join();
  }
}
class Attachment {
  DesertedServer server;
  AsynchronousSocketChannel client;
  ByteBuffer buffer;
  SocketAddress clientAddr;
  boolean isRead;
}

class ConnectionHandler implements
    CompletionHandler<AsynchronousSocketChannel, Attachment> {
  @Override
  public void completed(AsynchronousSocketChannel client, Attachment attach) {
    try {
      SocketAddress clientAddr = client.getRemoteAddress();
      System.out.format("Accepted a  connection from  %s%n", clientAddr);
      attach.server.getServerSock().accept(attach, this);
      ReadWriteHandler rwHandler = new ReadWriteHandler();
      Attachment newAttach = new Attachment();
      newAttach.server = attach.server;
      newAttach.client = client;
      newAttach.buffer = ByteBuffer.allocate(2048);
      newAttach.isRead = true;
      newAttach.clientAddr = clientAddr;
      client.read(newAttach.buffer, newAttach, rwHandler);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void failed(Throwable e, Attachment attach) {
    System.out.println("Failed to accept a  connection.");
    e.printStackTrace();
  }
}

class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {
  @Override
  public void completed(Integer result, Attachment attach) {
    if (result == -1) {
      try {
        attach.client.close();
        System.out.format("Stopped   listening to the   client %s%n",
            attach.clientAddr);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      return;
    }

    if (attach.isRead) {
      Message m = processReadToMessage(attach);
      try {
		processMessage(attach,m);
	    attach.isRead = true;
	    attach.buffer.clear();
	    attach.client.read(attach.buffer, attach, this);
	} catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	}
    }
  }
  
  public Message processReadToMessage(Attachment attach){
	  // you do a flip reading it to do get operations so it will write out up to that point
	  // it is ready for getting what was in it.
      attach.buffer.flip();
      Message msg = Message.byteBufferToMsg(attach.buffer);
      System.out.format("Client at  %s  says: %s%n", attach.clientAddr,
          msg.toString());
      return msg;
  }
  
  public void processMessage(Attachment attach,Message m) throws InterruptedException, ExecutionException{
	  switch(m.getType()){
	case USERNAME:
		processUsername(attach,m);
		break;
	case CHALLENGE:
		processChallenge(attach,m);
		break;
	case INGAME:
		processInGame(attach,m);
		break;
	// returns a list of games
	// a server should never receive a lobby message
	case NORMAL:
		break;
	case LOBBY:
		break;
	case BACK:
		break;
	default:
		break;
	  
	  }
  }
  
  public void processInGame(Attachment attach,Message m) throws InterruptedException, ExecutionException {
	  InGameMessage igm = (InGameMessage) m;
	  String playerName = attach.server.getUserFromSocket(attach.client);
	  assert(igm.getFrom() == playerName);
	  
	  String oppName = attach.server.getPlayersOppName(playerName);
	  assert(igm.getTo() == oppName);
	  
	  AsynchronousSocketChannel oppSock = attach.server.getPlayersOppSock(playerName);
	  oppSock.write(m.msgToByteBuffer()).get();
  }
  
  public void processUsername(Attachment attach,Message m) throws InterruptedException, ExecutionException{
	  UsernameMessage um = (UsernameMessage) m;
	  String username = um.getUsername();
	  attach.server.addUsername(username, attach.client);
	  ArrayList<String> users = attach.server.getUsers();
	  LobbyMessage lm = new LobbyMessage(users);
	  ByteBuffer bb = lm.msgToByteBuffer();
	  attach.client.write(bb).get();
  }
  
  public void processChallenge(Attachment attach,Message m) throws InterruptedException, ExecutionException{
	  String oppName = attach.server.getChallenger(attach.client);
	  String yourName = attach.server.getUserFromSocket(attach.client);
	  AsynchronousSocketChannel clientopp = attach.server.getSockFromUser(oppName);
	  ChallengeMessage cm = new ChallengeMessage(yourName,oppName,yourName);
	  ByteBuffer bb = cm.msgToByteBuffer();
	  
	  // call a synchronous write
      clientopp.write(bb).get();
  }

  @Override
  public void failed(Throwable e, Attachment attach) {
    e.printStackTrace();
  }
}