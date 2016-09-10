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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import desertedServer.Client.ClientStage;
import networkUtils.BackMessage;
import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.DraftMessage;
import networkUtils.InGameMessage;
import networkUtils.LobbyMessage;
import networkUtils.Message;
import networkUtils.UsernameMessage;

public class DesertedServer {
	
	AsynchronousServerSocketChannel server;
	ConcurrentHashMap<Client,Client> p1vp2;
	ConcurrentHashMap<String,Client> userNameToConnect;
	Queue<Client> waitingQueue;
	
	public DesertedServer(){
		waitingQueue = new LinkedBlockingQueue<Client>();
		p1vp2 = new ConcurrentHashMap<Client,Client>();
		userNameToConnect = new ConcurrentHashMap<String,Client>();
	}
	
	public void addUsername(String username, Client socket){
		userNameToConnect.put(username, socket);
	}
	
	public DesertedServer(AsynchronousServerSocketChannel serverSock){
		this();
		this.server = serverSock;
	}
	
	public void close(Client client) throws IOException{
		Client opp = null;
		if (client != null){
			userNameToConnect.remove(client);
			if (client.getSock().isOpen()){
				client.getSock().close();
			}
			switch(client.getStage()){
			case DRAFT:
				opp = p1vp2.get(client);
				if (opp != null){
					p1vp2.remove(client);
					p1vp2.remove(opp);
					this.close(opp);
				}
				break;
			case INGAME:
				opp = p1vp2.get(client);
				if (opp != null){
					System.out.println("are we here closing");
					p1vp2.remove(client);
					p1vp2.remove(opp);
					this.close(opp);
				}
				break;
			case WAITING:
				synchronized(waitingQueue){
					waitingQueue.poll();
				}
				break;
			default:
				break;
			}
		}
	}
	
	public Client getPlayersOppSock(String playerUsername){
		Client sockPlayer = userNameToConnect.get(playerUsername);
		Client sockOpp = p1vp2.get(sockPlayer);
		return sockOpp;
	}
	
	public String getPlayersOppName(String playerUsername){
		Client sockPlayer = userNameToConnect.get(playerUsername);
		Client sockOpp = p1vp2.get(sockPlayer);
		String oppName = this.getUserFromSocket(sockOpp);
		return oppName;
	}
	
	public Client getSockFromUser(String username){
		return userNameToConnect.get(username);
	}
	
	public String getUserFromSocket(Client user){
		return user.name;
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
	
	/** returns true if he connected first 
	 *  returns false if he connected second
	 * @param user
	 * @throws InterruptedException
	 */
	public Boolean getChallenger(Client user) throws InterruptedException{
		
		if (user.getStage() == null){
			Client opp = waitingQueue.peek();
			synchronized(waitingQueue){
				if (opp != null) {
					waitingQueue.poll();
					p1vp2.put(user, opp);
					p1vp2.put(opp, user);
					return false;
				}
				else {
					waitingQueue.add(user);
					System.out.println("we are here 1\n");
					return null;
				}
			}
		}
		else {
			assert(user.getStage() == ClientStage.WAITING);
			if (!p1vp2.containsKey(user)) {
				System.out.println("we are here 2\n");
				return null;
			} else {
				return true;
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
  Client client;
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
      newAttach.client = new Client(client);
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
	
	public void shutdownClient(Attachment attach) throws IOException{
		attach.server.close(attach.client);
	}
	
	
  @Override
  public void completed(Integer result, Attachment attach) {
    if (result == -1) {
      try {
    	// shutdown client
        this.shutdownClient(attach);
        
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
	    attach.client.getSock().read(attach.buffer, attach, this);
	} catch (InterruptedException | ExecutionException | IOException e) {
		e.printStackTrace();
	}
    }
  }
  
  public Message processReadToMessage(Attachment attach){
	  // you do a flip reading it to do get operations so it will write out up to that point
	  // it is ready for getting what was in it.
      Message msg = Message.byteBufferToMsg(attach.buffer);
      System.out.format("Client at  %s  says: %s%n", attach.clientAddr,
          msg.toString());
      return msg;
  }
  
  public void processMessage(Attachment attach,Message m) throws InterruptedException, ExecutionException, IOException{
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
		processBack(attach, m);
		break;
	case DRAFT:
		processDraft(attach, m);
	default:
		break;
	  
	  }
  }
  
  /** signal the opponent to shutdown **/
  public void processBack(Attachment attach,Message m) throws IOException{
	  BackMessage bm = (BackMessage) m;
	  attach.server.close(attach.client);
  }
  
  public void processInGame(Attachment attach,Message m) throws InterruptedException, ExecutionException {
  	System.out.println("INGAME MSG");
	  InGameMessage igm = (InGameMessage) m;
	  String playerName = attach.server.getUserFromSocket(attach.client);
	  assert(igm.getFrom() == playerName);
	  
	  String oppName = attach.server.getPlayersOppName(playerName);
	  assert(igm.getTo() == oppName);
	  
	  Client oppSock = attach.server.getPlayersOppSock(playerName);
	  oppSock.getSock().write(m.msgToByteBuffer()).get();
      attach.client.setClientStage(ClientStage.INGAME);
  }
  
  public void processDraft(Attachment attach,Message m) throws InterruptedException, ExecutionException {
	  DraftMessage igm = (DraftMessage) m;
	  String playerName = attach.server.getUserFromSocket(attach.client);
	  
	  String oppName = attach.server.getPlayersOppName(playerName);
	  
	  Client oppSock = attach.server.getPlayersOppSock(playerName);
	  oppSock.getSock().write(m.msgToByteBuffer()).get();
  }
  
  public void processUsername(Attachment attach,Message m) throws InterruptedException, ExecutionException{
	  UsernameMessage um = (UsernameMessage) m;
	  String username = um.getUsername();
	  attach.client.setName(username);
	  attach.server.addUsername(username, attach.client);
	  ArrayList<String> users = attach.server.getUsers();
	  processChallenge(attach,m);
//	  LobbyMessage lm = new LobbyMessage(users);
//	  ByteBuffer bb = lm.msgToByteBuffer();
//	  attach.client.getSock().write(bb).get();
  }
  
  public void processChallenge(Attachment attach,Message m) throws InterruptedException, ExecutionException{
	  System.out.println("we are in the challenge");
	  Boolean isFirst = attach.server.getChallenger(attach.client);
	  if (isFirst == null){
		  attach.client.setClientStage(ClientStage.WAITING);
		  ChallengeMessage cm = new ChallengeMessage();
		  ByteBuffer bb = cm.msgToByteBuffer();
		  attach.client.getSock().write(bb).get();
	  }
	  else {
		  String yourName = attach.server.getUserFromSocket(attach.client);
		  String oppName = attach.server.getPlayersOppName(yourName);
		  Client clientopp = attach.server.getSockFromUser(oppName);
		  ChallengeMessage cm = new ChallengeMessage(oppName,yourName,oppName,isFirst);
		  ByteBuffer bb = cm.msgToByteBuffer();
		  
		  // call a synchronous write
	      attach.client.getSock().write(bb).get();
	      attach.client.setClientStage(ClientStage.DRAFT);
	  }
  }

  @Override
  public void failed(Throwable e, Attachment attach) {
    e.printStackTrace();
  }
}