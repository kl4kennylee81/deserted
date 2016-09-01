package desertedServer;

import networkUtils.Connection;
import networkUtils.Handler;

public class GameHandler extends Handler{
	
	Connection c;
	GameState gameState;

	public GameHandler(Connection c,GameState gState) {
		super(c);
		gameState = gState;
	}
	
	@Override
    public void run() {
    	while(c.isAlive()){
    		
    		
    		
    		
    		
    		
    		
    	}
    }

}
