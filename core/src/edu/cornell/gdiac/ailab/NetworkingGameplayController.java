package edu.cornell.gdiac.ailab;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.CharacterActions.CharacterAction;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.InGameMessage;
import networkUtils.Message;

public class NetworkingGameplayController extends GameplayController {

	Connection connection;
	boolean isFirst;
	String from;
	String to;
	ActionDescription boxDrawer;
	
	boolean playAgain;
	
	public NetworkingGameplayController(MouseOverController moc,
			CompletionMenuController cmc, PauseMenuController pmc, FileHandle file,
			int fileNum, boolean isTutorial) {
		super(moc, cmc, pmc, file, fileNum, isTutorial);
		canPause = false;
		playAgain = false;
		boxDrawer = new ActionDescription();
	}
	
	@Override
	public void updateCompletionMenu() {
		compMenuController.update();
		if (compMenuController.doneSelecting){
			inGameState = InGameState.DONE;
			playAgain = compMenuController.selected.equals("Next Level");
			compMenuController.reset();
			CompletionScreen.getInstance().reset();
		}
	}
	
	public boolean onlinePlayerWon(){
		return (isFirst && super.playerWon()) || (!isFirst && super.playerLost());
	}
	
	@Override
	public void drawPlayerNames(GameCanvas canvas) {
		String leftName = isFirst ? to : from;
		String rightName = isFirst ? from : to;
		
		float box_width = 0.07f * canvas.width;
		float box_height = 0.05f * canvas.height;
		float box_y = 0.76f * canvas.height;
		float box_left_x = 0.075f*canvas.width-box_width/2;
		float box_right_x = 0.925f*canvas.width-box_width/2;
		
		boxDrawer.drawEmpty(canvas, box_left_x, box_y, box_width, box_height, Color.WHITE);
		boxDrawer.drawEmpty(canvas, box_right_x, box_y, box_width, box_height, Color.WHITE);
		
		float text_left_x = 0.075f*canvas.width;
		float text_right_x = 0.925f * canvas.width;
		float text_y = 0.7975f * canvas.height;
		canvas.drawCenteredText(leftName, text_left_x, text_y, Color.WHITE);
		canvas.drawCenteredText(rightName, text_right_x, text_y, Color.WHITE);
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
		boolean messageSent = bytes_written != null;
		if (messageSent) {
			actionBarController.isPlayerSelection = false;
		}
		return messageSent;
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
			actionBarController.isNetworkingOpponentSelection = false;
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
	
	@Override
	public void drawGameOver(GameCanvas canvas){
		if (this.gameOver()){
			CompletionScreen cs = CompletionScreen.getInstance();
			cs.setIsWin(onlinePlayerWon());
			cs.setTopText("Play Again");
			cs.draw(canvas);
		}
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public void setupGame(boolean isFirst, String from, String to) {
		this.isFirst = isFirst;
		this.from = from;
		this.to = to;
	}
}
