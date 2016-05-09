package edu.cornell.gdiac.ailab;

public class CurrentHighlight {
	double xPos;
	double yPos;
	double width;
	double height;
	String arrow;
	boolean isChar;
	boolean isSquare;

	public CurrentHighlight(double xPos, double yPos, double width, double height, String arrow, boolean isChar, boolean isSquare) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.arrow = arrow;
		this.isChar = isChar;
		this.isSquare = isSquare;
	}
}