package desertedClient;

import java.net.*;
import java.io.*;

import networkUtils.Connection;
import networkUtils.Message;
import javax.swing.JOptionPane;


public class DesertedClient {
	
	public DesertedClient(){
		
	}
    
	public static void main(String[] args) throws IOException {
        String serverAddress = JOptionPane.showInputDialog(
            "Enter IP Address of a machine that is\n" +
            "running the date service on port 9090:");
        Socket s = new Socket(serverAddress, 9090);
        BufferedReader input =
            new BufferedReader(new InputStreamReader(s.getInputStream()));
        String answer = input.readLine();
        JOptionPane.showMessageDialog(null, answer);
        System.exit(0);
    }
}
