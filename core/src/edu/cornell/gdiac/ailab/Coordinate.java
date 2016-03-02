package edu.cornell.gdiac.ailab;

public class Coordinate {

	int x;
	int y;
	
	public Coordinate(int x,int y){
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString(){
		return String.format("%d,%d",x,y);
	}
}
