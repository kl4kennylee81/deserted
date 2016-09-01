package desertedServer;

import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.io.*;

import networkUtils.Connection;
import networkUtils.Handler;
import networkUtils.Message;


public class DesertedServer {
	
	
	HashMap<String,Connection> UserToConnection;
	
	Queue<Connection> connectQ;
	
	public DesertedServer(){
		UserToConnection = new HashMap<String,Connection>();
		connectQ = new LinkedList<Connection>();
	}

    public static void main(String[] args) throws IOException {
    	System.out.println("Server has Started");
        ServerSocket listener = new ServerSocket(9090);
        try {
            while (true) {
                Socket socket = listener.accept();
                try {
                	DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeBytes(new Date().toString());
                } finally {
                    socket.close();
                }
            }
        }
        finally {
            listener.close();
        }
    }		
}
