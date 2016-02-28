/*
 * MeshDataInterlaced.java
 *
 * OpenGL supports two primary mesh formats: interlaced and non-interlaced. In interlaced
 * the position, texture, and normal data are all in one array.  In non-interlaced, they
 * are in different arrays.
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to support LibGDX AssetManager, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Class provides an interlaced OpenGL buffer
 */
public class MeshDataInterlaced {
	/** Interlaced vertex data */
	public FloatBuffer vertices;
	/** Integer indices in the domain [0, Vertex count) */
	public IntBuffer indices;

	/** Number of components in a vertex */
	public int vertexComponentCount;
	
	/** Number of vertices used in the mesh */
	public int vertexCount;
	/** Number of indices used in the mesh (should be a multiple of 3) */
	public int indexCount;
	
	/** 
	 * Creates an empty mesh.
	 */
	public MeshDataInterlaced() {
		vertices = null;
		indices  = null;

		vertexComponentCount = 0;
		vertexCount = 0;
		indexCount  = 0;
	}
	
	/**
	 * Returns true if this object contains the necessary data to visualize a mesh
	 * 
	 * @return true if this object contains the necessary data to visualize a mesh
	 */
	public boolean hasData() {
		return vertexCount >= 3 && indexCount >= 3 && 
				vertices != null && indices != null &&
				vertices.capacity() >= (vertexCount * 3) && 
				indices.capacity() >= indexCount;
	}
}