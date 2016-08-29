package networkUtils;

import java.net.*;
import java.io.*;

public class Connection {

	Socket sock;
    BufferedReader reader;
    DataOutputStream writer;
    
    public Connection(Socket s) throws IOException{
    	sock = s;
    	reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
    	writer = new DataOutputStream(s.getOutputStream());
    }
	
	public int getRemotePort(){
		return sock.getPort();
	}
	
	public InetAddress getRemoteAddress(){
		return sock.getInetAddress();
	}
	
	public InetAddress getLocalAddress(){
		return sock.getLocalAddress();
	}
	
	public int getLocalPort(){
		return sock.getLocalPort();
	}
	
	public void closeConnection(){
		
	}
	
	public String read() throws IOException{
		return reader.readLine();
	}
	
	public int write(String m) throws IOException{
		int size_before = writer.size();
		writer.writeBytes(m);
		return writer.size() - size_before;
	}
}
