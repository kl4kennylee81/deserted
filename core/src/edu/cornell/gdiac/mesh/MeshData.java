/*
 * MeshData.java
 *
 * OpenGL supports two primary mesh formats: interlaced and non-interlaced. In interlaced
 * the position, texture, and normal data are all in one array.  In non-interlaced, they
 * are in different arrays.
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to support LibGDX AssetManager, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import java.nio.*;

/**
 * Class provides a non-interlaced OpenGL buffer
 */
public class MeshData {
	/** Vector3 components representing vertex positions (Mandatory) */
	public FloatBuffer positions;
	/** Normalized Vector3 components representing vertex normals (Optional) */
	public FloatBuffer normals;
	/** Vector2 components with values in the domain [0, 1] */
	public FloatBuffer uvs;
	/** Integer indices in the domain [0, Vertex Count) */
	public IntBuffer indices;

	/** Number of vertices used in the mesh */
	public int vertexCount;
	/** Number of indices used in the mesh (should be a multiple of 3) */
	public int indexCount;
	
	/** 
	 * Creates an empty mesh.
	 */
	public MeshData() {
		positions = null;
		normals   = null;
		uvs = null;
		
		indexCount  = 0;
		vertexCount = 0;
	}

	/**
	 * Returns true if this object contains the necessary data to visualize a mesh
	 * 
	 * @return true if this object contains the necessary data to visualize a mesh
	 */
	public boolean hasData() {
		return vertexCount >= 3 && indexCount >= 3 && 
				positions != null && indices != null &&
				positions.capacity() >= (vertexCount * 3) && 
				indices.capacity() >= indexCount;
	}
	
	/**
	 * Returns true if this mesh contains normals.
	 *
	 * @return true if this mesh contains normals.
	 */
	public boolean hasNormals() {
		return normals != null && normals.capacity() >= (vertexCount * 3);
	}

	/**
	 * Returns true if this mesh contains texture coordinates.
	 *
	 * @return true if this mesh contains texture coordinates.
	 */
	public boolean hasUVs() {
		return uvs != null && uvs.capacity() >= (vertexCount * 2);
	}
}