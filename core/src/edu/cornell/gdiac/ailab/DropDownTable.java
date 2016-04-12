package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class DropDownTable extends Table{
	private static Float SMALL_WIDTH=50f;
	private Float PADDING = 4f;
	private LinkedList<SelectBox<String>> boxes; 
	private Array<LinkedList<TextField>> additional;
	private String[] fieldLabels;
	//private Integer numAdditional;
	private String[] options;
	private Skin skin;
	private TextButton add;
	private TextButton remove;
	
	public DropDownTable(String[] ops, String[] fieldLabels) {
		
		options = ops;
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		boxes = new LinkedList<SelectBox<String>>();
		SelectBox<String> box = new SelectBox<String>(skin);
		boxes.add(box);
		box.setItems(ops);
		this.add(box).pad(PADDING);
		
		if (fieldLabels != null){
			this.fieldLabels = fieldLabels;
			//this.numAdditional = fieldLabels.length;
			additional = new Array<LinkedList<TextField>>(fieldLabels.length);
			
			for (String label : fieldLabels){
				LinkedList<TextField> list = new LinkedList<TextField>();
				additional.add(list);
				TextField field = new TextField("", skin);
				list.add(field);
				Label fieldLabel = new Label(label, skin);
				this.add(fieldLabel).pad(PADDING);
				this.add(field).width(SMALL_WIDTH).pad(PADDING);
			}
		}
		
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
		this.add(add).pad(PADDING);
		this.row();
		//this.debug();
	}
	
	private void removeLastRow() {
		add.remove();
		remove.remove();
		
		
		SelectBox<String> box = boxes.removeLast();
		box.remove();
		box.clear();
		if (additional != null){
			for(LinkedList<TextField> list : additional){
				TextField field = list.removeLast();
				field.remove();
				field.clear();
			}
		}
		repopulateTable();
	}
	
	private void repopulateTable() {
		this.clear();
		this.add(boxes.get(0)).pad(PADDING);
		if (fieldLabels != null){
			int k = 0;
			for (String label : fieldLabels){
				this.add(new Label(label, skin)).pad(PADDING);
				TextField field = additional.get(k).getFirst();
				this.add(field).width(SMALL_WIDTH).pad(PADDING);
				k++;
			}
			
			for (int i = 1; i < boxes.size(); i++) {
				this.row();
				this.add(boxes.get(i)).pad(PADDING);
				k=0;
				for (String label : fieldLabels){
					this.add(new Label(label, skin)).pad(PADDING);
					TextField field = additional.get(k).get(i);
					this.add(field).width(SMALL_WIDTH).pad(PADDING);
					k++;
				}
			}
		}else{
			for (int i = 1; i < boxes.size(); i++) {
				this.row();
				this.add(boxes.get(i)).pad(PADDING);
			}
		}		
		this.add(add).pad(PADDING);
		if (boxes.size() > 1){
			this.add(remove).pad(PADDING);
		}
		this.row();
	}
	
	private void addNewRow() {
		add.remove();
		remove.remove();
		
		SelectBox<String> box = new SelectBox<String>(skin);
		boxes.add(box);
		box.setItems(options);
		
		this.add(box).pad(PADDING);
		if (fieldLabels != null){
			for (int i=0; i < fieldLabels.length; i++){
				this.add(new Label(fieldLabels[i], skin)).pad(PADDING);
				TextField field = new TextField("", skin);
				additional.get(i).add(field);
				this.add(field).width(SMALL_WIDTH).pad(PADDING);
			}
		}
		
		this.add(add).pad(PADDING);
		this.add(remove).pad(PADDING);
		this.row();
		
	}
	
	public void reset() {
		boxes.clear();
		if (fieldLabels != null){
			for (LinkedList<TextField> list : additional){
				list.clear();
			}
		}
		this.clear();
	}
	
	public void setValues(String[] values, String[][] additionalVals) {
		reset();
		if (values != null){
			setUpValue(values[0], formatFieldVals(additionalVals, 0));
			remove.remove();
			for (int i = 1; i < values.length ; i++) {
				setUpValue(values[i], formatFieldVals(additionalVals, 0));			
			}
		}else{
			setUpValue("", null);
			remove.remove();
		}
		
	}
	
	private String[] formatFieldVals(String[][] additionalVals, int level) {
		if (fieldLabels == null) {
			return null;
		}
		String[] fVals = new String[fieldLabels.length];
		int i = 0;
		for (String[] vals : additionalVals) {
			fVals[i] = vals[level];
			i++;
		}
		return fVals;
	}
	
	private void setUpValue(String value, String[] fieldVals) {
		addNewRow();
		SelectBox<String> box = boxes.getLast();
		box.setSelected(value);
		if (fieldVals != null) {
			int i = 0;
			for (LinkedList<TextField> list : additional) {
				TextField field = list.getLast();
				field.setText(fieldVals[i]);
				i++;
			}
		}
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
	
	public String[] getStringValues(){
		String[] vals = new String[boxes.size()]; 
		int i = 0;
		for (SelectBox<String> box : boxes){
			vals[i] = box.getSelected();
			i++;
		}
		return vals;
	}
	
	public Integer[] getIntFieldValues(int fieldIndex) {
		LinkedList<TextField> list = additional.get(fieldIndex);
		Integer[] vals = new Integer[list.size()];
		int i = 0;
		for (TextField field : list){
			vals[i] = Integer.parseInt(field.getText());
		}
		return vals;
	}
	
}
