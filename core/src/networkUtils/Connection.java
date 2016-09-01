package networkUtils;

import java.net.*;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Connection {
	
	private static AtomicInteger atomic_int = new AtomicInteger(0);
	
	int id;
	AsynchronousSocketChannel sock;
    InputStream reader;
    OutputStream writer;
    
    public Connection(AsynchronousSocketChannel s) throws IOException{
    	id = atomic_int.incrementAndGet();
    	sock = s;
    	reader = Channels.newInputStream(s);
    	writer = Channels.newOutputStream(s);
    }
	
    public int getId(){
    	return id;
    }
    
//    public boolean isAlive(){
//    	return sock.isConnected();
//    }
//    
//	public int getRemotePort(){
//		return sock.getPort();
//	}
//	
//	public InetAddress getRemoteAddress(){
//		return sock.getInetAddress();
//	}
//	
//	public InetAddress getLocalAddress(){
//		return sock.getLocalAddress();
//	}
//	
//	public int getLocalPort(){
//		return sock.getLocalPort();
//	}
//	
//	public void closeConnection() throws IOException{
//		sock.close();
//	}
//	
//	public String read() throws IOException{
//		return reader.readLine();
//	}
//	
//	public int write(String m) throws IOException{
//		int size_before = writer.size();
//		writer.writeBytes(m);
//		return writer.size() - size_before;
//	}
//}
