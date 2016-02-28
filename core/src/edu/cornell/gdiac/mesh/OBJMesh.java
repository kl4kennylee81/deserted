/*
 * OBJMesh.java
 *
 * OBJ is a special format for storing meshes.  This stores the data in a "raw" format 
 * after it has been read to a file but before it is converted to an OpenGL mesh format.
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to support LibGDX AssetManager, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import java.util.ArrayList;
import com.badlogic.gdx.math.*;

/**
 * Class to store mesh data represented in OBJ format
 * 
 * Note that "unique" is used with quotation marks because it is preferred that all data 
 * found within the lists is unique.
 */
public class OBJMesh {

	/** List of "unique" positions */
	public final ArrayList<Vector3> positions;
	/** List of "unique" texture coordinates */
	public final ArrayList<Vector2> uvs;
	/** List of "unique" normals */
	public final ArrayList<Vector3> normals;
	
	/**
	 * List of "unique" vertex indices
	 *
	 * X-component indexes into positions list
	 * Y-component indexes into texture coordinates list
	 * Z-component indexes into normals list
	 */
	public final ArrayList<Vector3i> vertices;

	/**
	 * List of of "unique" triangles from the vertices
	 *
	 * Each component indexes into vertex list with triangle orientation being X->Y->Z
	 */
	public final ArrayList<Vector3i> triangles;
	
	/**
	 * Creates a new, empty, OBJMesh
	 */
	public OBJMesh() {
		positions = new ArrayList<Vector3>();
		uvs = new ArrayList<Vector2>();
		normals = new ArrayList<Vector3>();
		vertices = new ArrayList<Vector3i>();
		triangles = new ArrayList<Vector3i>();
	}

	/**
	 * Returns true if this contains the necessary data to visualize a mesh.
	 *
	 * @return true if this contains the necessary data to visualize a mesh.
	 */
	public boolean hasData() {
		return positions.size() >= 3 && triangles.size() > 0 && vertices.size() >= 3;
	}
	
	/**
	 * Returns true if this mesh has texture coordinates.
	 *
	 * @return true if this mesh has texture coordinates.
	 */
	public boolean hasUVs() {
		return uvs.size() > 0;
	}
	
	/**
	 * Returns true if this mesh has normals.
	 *
	 * @return true if this mesh has normals.
	 */
	public boolean hasNormals() {
		return normals.size() > 0;
	}
	
	/**
	 * Convert the mesh into a non-interlaced format for OpenGL
	 *
	 * @param flipV whether to flip the V texture coordinate
	 *
	 * @return the mesh as a non-interlaced format for OpenGL
	 */
	public MeshData flatten(boolean flipV) {
		if (!hasData()) return null;
		
		MeshData meshdata = new MeshData();
		meshdata.vertexCount = vertices.size();
		meshdata.indexCount = triangles.size() * 3;
		
		meshdata.positions = BufferTools.createFloatBuffer(meshdata.vertexCount * 3);
		for (Vector3i vert : vertices) {
			meshdata.positions.put(positions.get(vert.x).x);
			meshdata.positions.put(positions.get(vert.x).y);
			meshdata.positions.put(positions.get(vert.x).z);
		}
		
		meshdata.indices = BufferTools.createIntBuffer(meshdata.indexCount);
		for (Vector3i tri : triangles) {
			meshdata.indices.put(tri.x);
			meshdata.indices.put(tri.y);
			meshdata.indices.put(tri.z);
		}
		
		if (hasUVs()) {
			meshdata.uvs = BufferTools.createFloatBuffer(meshdata.vertexCount * 2);
			for (Vector3i vert : vertices) {
				meshdata.uvs.put(uvs.get(vert.y).x);
				float v = uvs.get(vert.y).y;
				meshdata.uvs.put(flipV ? (1 - v) : v);
			}
		}
		
		if (hasNormals()) {
			meshdata.normals = BufferTools.createFloatBuffer(meshdata.vertexCount * 3);
			for (Vector3i vert : vertices) {
				meshdata.normals.put(normals.get(vert.z).x);
				meshdata.normals.put(normals.get(vert.z).y);
				meshdata.normals.put(normals.get(vert.z).z);
			}
		}
		
		return meshdata;
	}
	
	/**
	 * Convert the mesh into a interlaced format for OpenGL
	 *
	 * @param flipV whether to flip the V texture coordinate
	 *
	 * @return the mesh as a interlaced format for OpenGL
	 */
	public MeshDataInterlaced interlace(boolean flipV) {
		if (!hasData()) return null;
		
		MeshDataInterlaced meshdata = new MeshDataInterlaced();
		meshdata.vertexCount = vertices.size();
		meshdata.indexCount = triangles.size() * 3;
		meshdata.vertexComponentCount = 3;
		if(hasNormals()) meshdata.vertexComponentCount += 3;
		if(hasUVs()) meshdata.vertexComponentCount += 2;
		
		meshdata.vertices = BufferTools.createFloatBuffer(meshdata.vertexCount * meshdata.vertexComponentCount);
		for (Vector3i vert : vertices) {
			meshdata.vertices.put(positions.get(vert.x).x);
			meshdata.vertices.put(positions.get(vert.x).y);
			meshdata.vertices.put(positions.get(vert.x).z);
			if(hasUVs()) {
				meshdata.vertices.put(uvs.get(vert.y).x);
				float v = uvs.get(vert.y).y;
				meshdata.vertices.put(flipV ? (1 - v) : v);
			}
			if(hasNormals()) {
				meshdata.vertices.put(normals.get(vert.z).x);
				meshdata.vertices.put(normals.get(vert.z).y);
				meshdata.vertices.put(normals.get(vert.z).z);
			}
		}
		
		meshdata.indices = BufferTools.createIntBuffer(meshdata.indexCount);
		for (Vector3i tri : triangles) {
			meshdata.indices.put(tri.x);
			meshdata.indices.put(tri.y);
			meshdata.indices.put(tri.z);
		}
		
		return meshdata;
	}
}