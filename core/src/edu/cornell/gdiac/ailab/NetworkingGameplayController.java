package edu.cornell.gdiac.ailab;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;

import edu.cornell.gdiac.ailab.CharacterActions.CharacterAction;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.InGameMessage;
import networkUtils.Message;

public class NetworkingGameplayController extends GameplayController {

	Connection connection;
	String from;
	String to;
	
	public NetworkingGameplayController(MouseOverController moc,
			CompletionMenuController cmc, PauseMenuController pmc, FileHandle file,
			int fileNum, boolean isTutorial) {
		super(moc, cmc, pmc, file, fileNum, isTutorial);
		canPause = false;
	}
	
	@Override
	public boolean handleSelectionDone() {
		CharacterActions ca = new CharacterActions();
		for (Character c : characters) {
			if (!c.isAI && !c.isNetworkingOpponent) {
				ca.addCharacterAction(c.id, c.queuedActions);
			}
		}
		Message m = new InGameMessage(from, to, ca);
		Integer bytes_written = null;
		try {
			bytes_written = connection.write(m);
		} catch (Exception e) {
		}
		return bytes_written != null;
	}
	
	@Override
	public void handleWaiting() {
		String s = null;
		try {
			s = connection.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (s != null) {
			Message m = Message.jsonToMsg(s);
//			try {
				InGameMessage igm = (InGameMessage) m;
				CharacterActions ca = igm.getCharacterActions();
				for (CharacterAction sentChar : ca.charActions) {
					for (Character myChar : this.characters) {
						if (sentChar.charId == myChar.id && myChar.isNetworkingOpponent) {
							myChar.queuedActions = sentChar.convertToActionGameNodes(myChar.getAvailableActions());
						}
					}
				}
				if (actionBarController.isPlayerSelection){
					inGameState = InGameState.SELECTION;
				} else {
					inGameState = InGameState.NORMAL;
				}
//			}
//			catch (ClassCastException e){
//				System.out.println("are we here\n");
//				return;
//			}
		}
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public void setupGame(Boolean isFirst, String from, String to) {
		//temp character setting
		for (Character c : characters) {
			if (isFirst) {
				c.isNetworkingOpponent = c.isAI;
			} else {
				c.isNetworkingOpponent = !c.isAI;
			}
			c.isAI = false;
		}
		this.from = from;
		this.to = to;
	}
}
