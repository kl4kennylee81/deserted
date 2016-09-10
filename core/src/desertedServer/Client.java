package desertedServer;

import java.nio.channels.AsynchronousSocketChannel;

public class Client {
	
	public enum ClientStage {WAITING,DRAFT,INGAME};

	String name;
	AsynchronousSocketChannel sock;
	ClientStage stage;
	
	public Client(AsynchronousSocketChannel sock){
		this.name = "";
		this.sock = sock;
		this.stage = null;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setClientStage(ClientStage stage){
		this.stage = stage;
	}
	
	public ClientStage getStage(){
		return this.stage;
	}
	
	public AsynchronousSocketChannel getSock(){
		return this.sock;
	}
}
