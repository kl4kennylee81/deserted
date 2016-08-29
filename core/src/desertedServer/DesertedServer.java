package desertedServer;

import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.io.*;

import networkUtils.Connection;
import networkUtils.Handler;
import networkUtils.Message;


public class DesertedServer {
	
	
	HashMap<String,Integer> idToIp;
	
	
	public DesertedServer(){
		idToIp = new HashMap<String,Integer>();
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
