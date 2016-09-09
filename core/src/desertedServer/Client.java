package desertedServer;

import java.nio.channels.AsynchronousSocketChannel;

public class Client {
	
	public enum ClientStage {WAITING,DRAFT,INGAME};

	String name;
	AsynchronousSocketChannel sock;
	ClientStage stage;
	
	public Client(AsynchronousSocketChannel sock,ClientStage stage){
		this.name = "";
		this.sock = sock;
		this.stage = stage;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public ClientStage getStage(){
		return this.stage;
	}
	
	public AsynchronousSocketChannel getSock(){
		return this.sock;
	}
}
