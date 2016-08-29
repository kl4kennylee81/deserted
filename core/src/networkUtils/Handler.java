package networkUtils;

public abstract class Handler extends Thread {

	/** There will be a game handler and lobby handler **/
	Connection connection;
	
	public Handler(Connection c) {
		connection = c;
	}

    public void run() {
    }

}
