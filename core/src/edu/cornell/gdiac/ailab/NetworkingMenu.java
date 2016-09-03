package edu.cornell.gdiac.ailab;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class NetworkingMenu {
	private Float OBJ_WIDTH = 400f;
	private Float OBJECT_HEIGHT = 20f;
	private Float PADDING = 50f;
	
	ArrayList<Option> options;
	String username;
	Stage stage;
	Table table;
	TextField usernameField;
	TextButton submit;
	TextButton back;
	
	boolean goBack;
	boolean goNext;
	
	private boolean submitClicked;
	private boolean backClicked;
	
	public NetworkingMenu() {
		stage = new Stage();
		table = new Table();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Label usernameLabel = new Label("Username:", skin);
		usernameField = new TextField("", skin);
		table.add(usernameLabel);
		table.add(usernameField).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		submit = new TextButton("Submit", skin);
		back = new TextButton("Back", skin);
		table.row();
		table.add(back);
		table.add(submit);
		table.setFillParent(true);
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
		submitClicked = false;
		backClicked = false;
		goBack = false;
		goNext = false;
	}
	
	public void update() {
		if (submitWasClicked() && usernameField.getText().length() > 0) {
			username = usernameField.getText();
			stage.dispose();
			goNext = true;
		}
		if (backWasClicked()) {
			stage.dispose();
			goBack = true;
		}
	}
	
	public boolean submitWasClicked() {
		if (submit.isPressed()){
			submitClicked = submit.isPressed();
		}
		if (InputController.pressedLeftMouse() &&submitClicked){
			submitClicked = false;
			return true;
		}
		return false;
	}
	
	public boolean backWasClicked() {
		if (back.isPressed()){
			backClicked = back.isPressed();
		}
		if (InputController.pressedLeftMouse() && backClicked){
			backClicked = false;
			return true;
		}
		return false;
	}
	
	public void draw() {
		stage.act();
		stage.draw();
	}
	
	public boolean goBack() {
		return goBack;
	}
	
	public boolean goNext() {
		return goNext;
	}
}
