package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;

public class Animation {
	/** Name of animation */
	String name;
	/** Film strip for animation */
	FilmStrip filmStrip;
	/** List of segments that divide animation */
	List<Segment> segments;
	
	/** A section of the film strip for an individual animation */
	public class Segment {
		/** Index of starting frame in FilmStrip */
		int startingIndex;
		/** Total number of frames in this segment */
		int length;
		/** Duration of each frame */
		Integer[] frameLengths;
		
		public Segment(int startingIndex, List<Integer> frameData){
			if (frameData == null){
				return;
			}
			this.startingIndex = startingIndex;
			this.length = frameData.size();
			frameLengths = new Integer[length];
			frameData.toArray(frameLengths);
		}
		
	}
	
	public Animation(String name, Texture texture, int rows, int cols, int size){
		this.name = name;
		this.filmStrip = new FilmStrip(texture, rows, cols, size);
		
		segments = new ArrayList<Segment>();
	}	
	
	/** Add a new segment with a given starting index and frame data */
	public void addSegment(int startingIndex, List<Integer> frameLengths){
		segments.add(new Segment(startingIndex, frameLengths));
	}
}
