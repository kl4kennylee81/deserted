package desertedClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.InGameMessage;
import networkUtils.LobbyMessage;
import networkUtils.Message;
import networkUtils.NormalMessage;
import networkUtils.UsernameMessage;


public class DesertedClient2 {
  
	String username;
	String oppUsername;
	Connection connect;
	GameState gstate;
	
	boolean sentUsername;
	boolean sentChallenge;
	boolean sentMessage;
	NetworkState nstate;
	
	public enum GameState {PRE,LOBBY,INGAME}
	
	public enum NetworkState {READ,WRITE,NORMAL}
	
	public DesertedClient2(Connection c,boolean sendFirst){
	  connect = c;
	  oppUsername = "";
	  gstate = GameState.PRE;
	  nstate = NetworkState.NORMAL;
	  sentUsername = false;
	  sentChallenge = false;
	  
	  sentMessage = sendFirst;
    }
	
	public void processState() throws Exception{
		switch (gstate){
		case INGAME:
			processInGameState();
			break;
		case LOBBY:
			processLobbyState();
			break;
		case PRE:
			processPreState();
			break;
		default:
			break;
		}
	}
	
	/** Deserted client 1 read then writes 
	 *  While deserted client 2 writes then reads **/
	public void processInGameState() throws Exception {
		switch (nstate){
		case NORMAL:
			if (this.sentMessage){
				String s = this.connect.read();
				System.out.println("haha");
				if (s != null){
					Message m = Message.jsonToMsg(s);
					InGameMessage igm = (InGameMessage) m;
					String msg = igm.toString();
					System.out.println(msg);
					this.sentMessage = false;
				}
			}
			else{
				String msg = "Blaze it 420 from the client2\n";
				InGameMessage igm = new InGameMessage(this.username,this.oppUsername,msg);
				this.sentMessage = true;
				this.write(igm);
			}
			break;
		case READ:
			if (this.connect.isDoneReading()){
				this.nstate = NetworkState.NORMAL;
			}
			break;
		case WRITE:
			if (this.connect.isDoneWriting()){
				this.nstate = NetworkState.NORMAL;
			}
			break;
		default:
			break;
		}
	}
	
	public void sendChallengeMsg() throws IOException, InterruptedException, ExecutionException{
		ChallengeMessage cm = new ChallengeMessage();
	    this.sentChallenge = true;
	    this.write(cm);
	}
	
	public void processLobbyState() throws Exception{
		switch (nstate){
		case NORMAL:
			if (this.sentChallenge){
				String s = this.connect.read();
				if (s != null){
					Message m = Message.jsonToMsg(s);
					ChallengeMessage cm = (ChallengeMessage) m;
					this.oppUsername = cm.getOpponent();
					System.out.println(oppUsername);
					this.gstate = GameState.INGAME;
				}
			}
			else{
				sendChallengeMsg();
			}
			break;
		case READ:
			if (this.connect.isDoneReading()){
				System.out.println("hi");
				this.nstate = NetworkState.NORMAL;
			}
			break;
		case WRITE:
			if (this.connect.isDoneWriting()){
				this.nstate = NetworkState.NORMAL;
			}
			break;
		default:
			break;
		}
	}
	
	public void sendUsernameMsg() throws Exception {
		    this.username = getTextFromUser();
		    Message msg = new UsernameMessage(this.username);
		    this.sentUsername = true;
		    this.write(msg);
	}
	
	public void write(Message m) throws IOException, InterruptedException, ExecutionException{
	    this.nstate = NetworkState.WRITE;
	    this.connect.write(m);
	}
	
	
	
	public void processPreState() throws Exception{
		switch (this.nstate){
		case NORMAL:
			if (this.sentUsername){
				String s = this.connect.read();
				if (s != null){
					Message m = Message.jsonToMsg(s);
					LobbyMessage lm = (LobbyMessage) m;
					this.gstate = GameState.LOBBY;
				}
			}
			else{
				sendUsernameMsg();
			}
			break;
		case READ:
			if (this.connect.isDoneReading()){
				this.nstate = NetworkState.NORMAL;
			}
			break;
		case WRITE:
			if (this.connect.isDoneWriting()){
				this.nstate = NetworkState.NORMAL;
			}
			break;
		default:
			break;
		
		}
	}
	
  public static void main(String[] args) throws Exception {

    Connection c = new Connection();
    DesertedClient2 dc = new DesertedClient2(c,false);
    dc.connect.connect("localhost", 8989);
    
    while(true){
//	    String s = getTextFromUser();
//	    Message msg = new NormalMessage(s);
//	    while (c.write(msg) == null){
//	    }
//	    
//	    Message response = c.readMsg();
//	    while(response == null){
//	    	response = c.readMsg();
//	    }
//	    count++;
//	    System.out.printf("%d:%s\n",count,response.getMessage());
    	dc.processState();
    }
    
    
    
    
  }

private static String getTextFromUser() throws Exception{
    System.out.print("Please enter a  message  (Bye  to quit):");
    BufferedReader consoleReader = new BufferedReader(
        new InputStreamReader(System.in));
    String msg = consoleReader.readLine();
    return msg;
  }
}