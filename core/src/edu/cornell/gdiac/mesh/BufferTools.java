/*
 * BufferTools.java
 *
 * Memory buffers differ across the various platforms. This abstraction provides
 * the correct native buffer access.
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to support LibGDX AssetManager, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import java.nio.*;
import com.badlogic.gdx.utils.BufferUtils;

/**
 * Wrapper for providing correct native memory within java
 */
public class BufferTools {
	/** What memory option have we chosen */
	private static final int MEM_TOGGLE = 2;
	
	/**
	 * Allocates a native memory buffer.
	 *
	 * @param size Number of bytes to allocate
	 *
	 * @return Buffer allocated, or null if allocation failed
	 */
	public static ByteBuffer createByteBuffer(int size) {
		switch (MEM_TOGGLE) {
		case 0: 
			return ByteBuffer.allocate(size);
		case 1: 
			return ByteBuffer.allocateDirect(size);
		default: 
			return BufferUtils.newByteBuffer(size);
		}
	}
	
	/**
	 * Allocates a native memory buffer for shorts
	 *
	 * @param size Number of shorts to allocate
	 *
	 * @return Buffer allocated, or null if allocation failed
	 */
	public static ShortBuffer createShortBuffer(int size) {
		return createByteBuffer(size * 2).asShortBuffer();
	}

	/**
	 * Allocates a native memory buffer for ints
	 *
	 * @param size Number of ints to allocate
	 *
	 * @return Buffer allocated, or null if allocation failed
	 */	
	public static IntBuffer createIntBuffer(int size) {
		return createByteBuffer(size * 4).asIntBuffer();
	}
	/**
	 * Allocates A Native Memory Buffer
	 * @param size Number Of Longs To Allocate
	 * @return A Memory Buffer
	 */
	public static LongBuffer createLongBuffer(int size) {
		return createByteBuffer(size * 8).asLongBuffer();
	}
	
	/**
	 * Allocates a native memory buffer for floats
	 *
	 * @param size Number of floats to allocate
	 *
	 * @return Buffer allocated, or null if allocation failed
	 */	
	public static FloatBuffer createFloatBuffer(int size) {
		return createByteBuffer(size * 4).asFloatBuffer();
	}
	
	/**
	 * Allocates a native memory buffer for doubles
	 *
	 * @param size Number of doubles to allocate
	 *
	 * @return Buffer allocated, or null if allocation failed
	 */	
	public static DoubleBuffer createDoubleBuffer(int size) {
		return createByteBuffer(size * 8).asDoubleBuffer();
	}
	
	/**
	 * Allocates a native memory buffer for chars
	 *
	 * @param size Number of chars to allocate
	 *
	 * @return Buffer allocated, or null if allocation failed
	 */	
	public static CharBuffer createCharBuffer(int size) {
		return createByteBuffer(size * 2).asCharBuffer();
	}
}