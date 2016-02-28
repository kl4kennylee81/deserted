/*
 * Vector3i.java
 *
 * The LibGDX math package does not contain integer precision vectors; all of its 
 * vectors are float precision.  This class provides the missing functionality.
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to conform to LibGDX math, 1/24/2015
 */
 package edu.cornell.gdiac.mesh;

import java.util.AbstractList;

/**
 * Class provides a 3-Component vector with integer precision.
 *
 * Attributes can be accessed by component or as a list.  This class mirrors the standard
 * LibGDX api.
 */
public class Vector3i extends AbstractList<Integer> implements Cloneable {
	/** The number of elements in this vector. */
	public static final int NUM_COMPONENTS = 3;
	
	/** The x-coordinate */
	public int x;
	/** The y-coordinate */
	public int y;
	/** The z-coordinate */
	public int z;

	/**
	 * Create a Vector3i from the given values.
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param y The z-coordinate
	 */
	public Vector3i(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Create a Vector3i that is a copy of the given vector
	 * 
	 * @param v The vector to copy
	 */
	public Vector3i(Vector3i v) {
		this(v.x, v.y, v.z);
	}
	
	/**
	 * Create a Vector3i with uniform values
	 * 
	 * @param f The value for all coordinates
	 */
	public Vector3i(int f) {
		this(f, f, f);
	}
	
	/**
	 * Create the zero Vector3i
	 */
	public Vector3i() {
		this(0);
	}

	/**
	 * Returns the string representation of this vector.
	 *
	 * @return the string representation of this vector
	 */
	@Override
	public String toString() {
		return "{"+x+", "+y+", "+z+"}i";
	}
	
	/**
	 * Sets the vector coordinates to those specified.
	 * 
	 * The vector is returned to allow chaining.
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param y The z-coordinate
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	/**
	 * Copies the coordinate from the given vector.
	 *
	 * The vector is returned to allow chaining.
	 * 
	 * @param v The vector to copy
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i set(Vector3i v) {
		return set(v.x, v.y, v.z);
	}

	/**
	 * Sets the coordinates of this vector to a single value
	 *
	 * The vector is returned to allow chaining.
	 * 
	 * @param f The coordinate value
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i set(int f) {
		return set(f, f, f);
	}
	
	/**
	 * Set this vector to the zero vector
	 * 
	 * The vector is returned to allow chaining.
	 * 
	 * @return This vector, for chaining purposes
	 */
	public Vector3i setZero() {
		return set(0);
	}

	/**
	 * Adds the given components to this vector
	 *
     * The vector is returned to allow chaining.
	 *
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param y The z-coordinate
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	/**
	 * Adds the given vector to this vector
	 *
     * The original vector is returned to allow chaining.
	 *
	 * @param v The vector to add
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i add(Vector3i v) {
		return add(v.x, v.y, v.z);
	}
	
	/**
	 * Adds the given value to the vector coordinates
	 *
     * The vector is returned to allow chaining.
	 *
	 * @param f The value to add
	 *
	 * @return This vector, for chaining purposes
	 */	
	public Vector3i add(int f) {
		return add(f, f, f);
	}
	
	/**
	 * Subtracts the given components from this vector
	 *
     * The vector is returned to allow chaining.
	 *
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param y The z-coordinate
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i sub(int x, int y, int z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	/**
	 * Subtracts the given vector from this vector
	 *
     * The original vector is returned to allow chaining.
	 *
	 * @param v The vector to subtract
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i sub(Vector3i v) {
		return sub(v.x, v.y, v.z);
	}
	
	/**
	 * Subtracts the given value from the vector coordinates
	 *
     * The vector is returned to allow chaining.
	 *
	 * @param f The value to subtract
	 *
	 * @return This vector, for chaining purposes
	 */	
	public Vector3i sub(int f) {
		return sub(f, f, f);
	}
	
	/**
	 * Scales this vector by a scalar
	 * 
	 * The vector is returned to allow chaining.
	 *	
	 * @param s The scalar
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i scl(int s) {
		x = s * x;
		y = s * y;
		z = s * z;
		return this;	
	}
	
	/**
	 * Scales this vector non-uniformly
	 *
     * The vector is returned to allow chaining.
	 *
	 * @param x The amount to scale the x-coordinate
	 * @param y The amount to scale the y-coordinate
	 * @param y The amount to scale the z-coordinate
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i scl(int x, int y, int z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	/**
	 * Scales this vector by another vector
	 * 
	 * The vector is returned to allow chaining.
	 *	
	 * @param s The scalar vector
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i scl(Vector3i v) {
		return scl(v.x, v.y, v.z);
	}

	/**
	 * Sets the vector to its piecewise absolute value.
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i abs() {
		if(x < 0) x = -x;
		if(y < 0) y = -y;
		if(z < 0) z = -z;
		return this;
	}
	
	/**
	 * Negates this vector coordinate wise.
	 *
	 * @return This vector, for chaining purposes
	 */
	public Vector3i neg() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	/**
	 * Returns the dot product between this and the other vector
	 *
	 * @param v The other vector
	 *
	 * @return the dot product between this and the other vector
	 */
	public int dot(Vector3i v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	/**
	 * Returns the squared euclidean length of the vector
	 *
	 * This method is faster than Vector3i.len() because it avoids calculating a 
	 * square root. It is useful for comparisons, but not for getting exact lengths, 
	 * as the return value is the square of the actual length.
	 *
	 * @return the squared euclidean length of the vector
	 */
	public int len2() {
		return dot(this);
	}

	/**
	 * Returns the euclidean length of the vector
	 *
	 * @return the euclidean length of the vector
	 */
	public float len() {
		return (float)Math.sqrt(len2());
	}

	/**
	 * Returns the squared distance between this and the other vector
	 *
	 * This method is faster than Vector3i.dst(Vector3i) because it avoids 
	 * calculating a square root. It is useful for comparisons, but not for getting 
	 * accurate distances, as the return value is the square of the actual distance.
	 *
	 * @param v The other vector
	 *
	 * @return the squared distance between this and the other vector
	 */
	public int dst2(Vector3i v) {
		int ox = x - v.x;
		int oy = y - v.y;
		int oz = z - v.z;
		return ox * ox + oy * oy + oz * oz;
	}
	
	/**
	 * Returns the distance between this and the other vector
	 *
	 * @param v The other vector
	 *
	 * @return the distance between this and the other vector
	 */
	public float dst(Vector3i v) {
		return (float)Math.sqrt(dst2(v));
	}
	
	/**
	 * Returns the angle in degrees of this vector (point) relative to the given vector. 
	 * 
	 * Angles are towards the positive y-axis (typically counter-clockwise.) between 
	 * -180 and +180
	 *
	 * @return the angle in degrees of this vector (point) relative to the given vector. 
	 */
	public float angle(Vector3i v) {
		return 180.0f*(float)(Math.acos(dot(v) / (len() * v.len()))/Math.PI);
	}
	
	/**
	 * Returns true if the two vectors are equal component-wise.
	 *
	 * @param v The other vector
	 *
	 * @return true if the two vectors are equal component-wise.
	 */
	public boolean equals(Vector3i v) {
		return x == v.x && y == v.y && z == v.z;
	}
	
	// INTEGER LIST methods
	/**
	 * Replaces the element at the specified position in this list with the specified 
	 * element (optional operation).
	 * 
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 *
	 * @return the element previously at the specified position
	 */
	@Override
	public Integer set(int index, Integer element) {
		int oldVal;
		switch (index) {
		case 0:
			oldVal = x;
			x = element;
			return oldVal;
		case 1:
			oldVal = y;
			y = element;
			return oldVal;
		case 2:
			oldVal = z;
			z = element;
			return oldVal;
		default: throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index index of the element to return
	 *
	 * @return the element at the specified position in this list.
	 */
	@Override
	public Integer get(int index) {
		switch(index) {
		case 0: return x;
		case 1: return y;
		case 2: return z;
		default: throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * Returns the number of elements in this collection. 
	 *
	 * This is identical to NUM_COMPONENTS
	 *
	 * @return the number of elements in this collection
	 */
	@Override
	public int size() {
		return NUM_COMPONENTS;
	}

	/**
	 * Creates a copy of this vector
	 *
	 * @return a copy of this vector
	 */
	@Override
	public Vector3i clone() {
		return new Vector3i(this);
	}
}