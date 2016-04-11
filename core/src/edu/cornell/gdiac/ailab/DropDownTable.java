package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class DropDownTable extends Table{
	private LinkedList<SelectBox<String>> boxes; 
	private Array<LinkedList<SelectBox<String>>> additional;
	private String[] options;
	private Skin skin;
	private TextButton add;
	private TextButton remove;
	
	public DropDownTable(String[] ops, String[][] additionalFields, Integer numAddFields) {
		
		options = ops;
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		boxes = new LinkedList<SelectBox<String>>();
		SelectBox<String> box = new SelectBox<String>(skin);
		boxes.add(box);
		box.setItems(ops);
		
		add = new TextButton("Add", skin);
		add.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				addNewRow();
			}
		});
		
		remove = new TextButton("Remove", skin);
		remove.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				removeLastRow();
			}
		});
		this.add(box);
		this.add(add);
		this.row();
		//this.debug();
	}
	
	private void removeLastRow() {
		add.remove();
		remove.remove();
		
		
		SelectBox<String> box = boxes.removeLast();
		box.remove();
		box.clear();
		repopulateTable();
	}
	
	private void repopulateTable() {
		this.clear();
		this.add(boxes.get(0));
		for (int i = 1; i < boxes.size(); i++) {
			this.row();
			this.add(boxes.get(i));
		}
		this.add(add);
		this.add(remove);
		this.row();
	}
	
	private void addNewRow() {
		add.remove();
		remove.remove();
		
		SelectBox<String> box = new SelectBox<String>(skin);
		boxes.add(box);
		box.setItems(options);
		
		this.add(box);
		this.add(add);
		this.add(remove);
		this.row();
		
	}
	
	public void reset() {
		boxes.clear();
		this.clear();
	}
	
	public void setValues(String[] values) {
		reset();
		if (values != null){
			setUpValue(values[0]);
			remove.remove();
			for (int i = 1; i < values.length ; i++) {
				setUpValue(values[i]);
			}
		}else{
			setUpValue("");
			remove.remove();
		}
		
	}
	
	public void setUpValue(String value) {
		addNewRow();
		SelectBox<String> box = boxes.getLast();
		box.setSelected(value);
	}
	
	public Integer[] getIdValues() {
		Integer[] vals = new Integer[boxes.size()]; 
		int i = 0;
		for (SelectBox<String> box : boxes){
			String selected = box.getSelected();
			String value = selected.substring(0, selected.indexOf(" "));
			Integer val = Integer.parseInt(value);
			vals[i] = val;
			i++;
		}
		return vals;
	}
	
}
