/*
 * OBJParser.java
 *
 * Static methods for parsing object files.  These methods do most of the work for
 * MeshLoader, reading in the file and computing the vertex information.
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to support LibGDX AssetManager, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import java.io.*;
import java.util.*;
import com.badlogic.gdx.math.*;

/**
 * Static methods to parse object files into MeshData.
 *
 * These methods do not create Mesh objects.  Those cannot be created until we have
 * an OpenGL context.  But they do load all of the information that we need to quickly
 * initialize a Mesh when a context is available.
 */
public class OBJParser {

	/** Default tolerance in the squared distance between positions */
	public static final float TOLERANCE_POSITION = 0.0001f;
	/** Default tolerance in the squared distance between texture coordinates */
	public static final float TOLERANCE_UV = 0.001f;
	/** Default tolerance in the dot product between normals */
	public static final float TOLERANCE_NORMAL = 0.990f;

	/**
	 * Creates an OBJMesh from the given data file
	 *
	 * This method never merges vertices.
	 *
	 * @param file The file with OBJ data
	 *
	 * @return A mesh object
	 */
	public static OBJMesh parse(String file) {
		return parse(file, false, false);
	}
	
	/**
	 * Creates an OBJMesh from the given data file
	 *
	 * This method never merges vertices.
	 *
	 * @param file The file with OBJ data
	 * @param discardTexCoords 	Whether to ignore texture coordinates
	 * @param discardNormals	Whether to ignore vertex normals
     *
	 * @return A mesh object
	 */
	public static OBJMesh parse(String file, boolean discardTexCoords, boolean discardNormals) {
		return parse(file, discardTexCoords, discardNormals, -1, -1, Float.MAX_VALUE);
	}
	
	/**
	 * Creates an OBJMesh from the given data file
	 *
	 * Vertices that are too close to one another are merged.
	 *
	 * @param file The file with OBJ data
	 *
	 * @return A mesh object
	 */
	public static OBJMesh parseWithMerging(String file) {
		return parseWithMerging(file, false, false);
	}	
	
	/**
	 * Creates an OBJMesh from the given data file
	 *
	 * Vertices that are too close to one another are merged.
	 *
	 * @param file The file with OBJ data
	 * @param discardTexCoords 	Whether to ignore texture coordinates
	 * @param discardNormals	Whether to ignore vertex normals
	 *
	 * @return A mesh object
	 */
	public static OBJMesh parseWithMerging(String file, boolean discardTexCoords, boolean discardNormals) {
		return parse(file, discardTexCoords, discardNormals, TOLERANCE_POSITION, TOLERANCE_UV, TOLERANCE_NORMAL);
	}
	
	/**
	 * Creates an OBJMesh from the given data file
	 *
	 * Vertices that are too close to one another are merged.
	 *
	 * @param file 				The file with OBJ data
	 * @param discardTexCoords 	Whether to ignore texture coordinates
	 * @param discardNormals	Whether to ignore vertex normals
	 * @param tPosSq			Tolerance to identify positions
	 * @param tUVSq				Tolerance to identify texture coordinates
	 * @param tNormDot			Tolerance to identify normals
	 *
	 * @return A mesh object
	 */
	public static OBJMesh parse(String file, boolean discardTexCoords, boolean discardNormals, float tPosSq, float tUVSq, float tNormDot) {
		try {
			return parse(new FileInputStream(file), discardTexCoords, discardNormals, tPosSq, tPosSq, tPosSq);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	/**
	 * Creates an OBJMesh from the given data file
	 *
	 * Vertices that are too close to one another are merged.
	 *
	 * @param file 				The file with OBJ data
	 * @param discardTexCoords 	Whether to ignore texture coordinates
	 * @param discardNormals	Whether to ignore vertex normals
	 * @param tPosSq			Tolerance to identify positions
	 * @param tUVSq				Tolerance to identify texture coordinates
	 * @param tNormDot			Tolerance to identify normals
	 *
	 * @return A mesh object
	 */
	public static OBJMesh parse(InputStream file, boolean discardTexCoords, boolean discardNormals, float tPosSq, float tUVSq, float tNormDot) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(file));

			// For minifying the mesh
			ArrayList<Integer> posInds = new ArrayList<Integer>(), uvInds = new ArrayList<Integer>(), normInds = new ArrayList<Integer>();
			PosComparer cmpPos = new PosComparer(tPosSq);
			UVComparer cmpUV = new UVComparer(tUVSq);
			NormComparer cmpNorm = new NormComparer(tNormDot);
			
			OBJMesh mesh = new OBJMesh();
			HashMap<Vector3i, Integer> vertMap = new HashMap<Vector3i, Integer>();
			
			// Read file line by line
			String line;
			while ((line = r.readLine()) != null) {
				Vector3 v3;
				Vector3i tri, vert;
				Vector2 v2;

				String[] splits = line.split("\\s+");
				if(splits[0].equals("v")) {
					// Add Position Component
					if(splits.length != 4) continue;
					v3 = new Vector3();
					v3.x = Float.parseFloat(splits[1]);
					v3.y = Float.parseFloat(splits[2]);
					v3.z = Float.parseFloat(splits[3]);
					posInds.add(indexOfUnique(mesh.positions, v3, cmpPos));
				} else if(splits[0].equals("vn")) {
					if(discardNormals) continue;
					
					// Add Normal Component
					if(splits.length != 4) continue;
					v3 = new Vector3();
					v3.x = Float.parseFloat(splits[1]);
					v3.y = Float.parseFloat(splits[2]);
					v3.z = Float.parseFloat(splits[3]);
					normInds.add(indexOfUnique(mesh.normals, v3, cmpNorm));
				} else if(splits[0].equals("vt")) {
					if(discardTexCoords) continue;
					
					// Add Texture Coordinate Component
					if(splits.length != 3) continue;
					v2 = new Vector2();
					v2.x = Float.parseFloat(splits[1]);
					v2.y = Float.parseFloat(splits[2]);
					uvInds.add(indexOfUnique(mesh.uvs, v2, cmpUV));
				} else if(splits[0].equals("f")) {
					// Add A Triangle
					if(splits.length != 4) continue;
					tri = new Vector3i();
					
					// Set The Triangle's 3 Vertex Indices
					for(int i = 0;i < 3;i++) {
						// Create The Vertex
						String[] vInds = splits[i + 1].split("/");
						vert = new Vector3i(0, 0, 0);
						switch (vInds.length) {
						case 1:
							vert.x = Integer.parseInt(vInds[0]);
							break;
						case 2:
							vert.x = Integer.parseInt(vInds[0]);
							if(!discardTexCoords) vert.y = Integer.parseInt(vInds[1]);
							break;
						case 3:
							vert.x = Integer.parseInt(vInds[0]);
							if(!discardTexCoords && !vInds[1].isEmpty()) vert.y = Integer.parseInt(vInds[1]);
							if(!discardNormals) vert.z = Integer.parseInt(vInds[2]);
							break;
						default:
							continue;
						}
						
						// It Was Using One-Based Indexing
						vert.sub(1, 1, 1);
						
						// Get The Unique Indices
						vert.x = posInds.get(vert.x);
						if(vert.y >= 0) vert.y = uvInds.get(vert.y);
						if(vert.z >= 0) vert.z = normInds.get(vert.z);
						
						// Get The Vertex Index For The Triangle
						if(vertMap.containsKey(vert)) {
							// Vertex Already Found
							tri.set(i, vertMap.get(vert));
						} else {
							// New Vertex Found
							vertMap.put(vert, mesh.vertices.size());
							tri.set(i, mesh.vertices.size());
							
							// Add New Vertex To Mesh
							mesh.vertices.add(vert);
						}
					}
					
					// Add The Triangle
					mesh.triangles.add(tri);
				}
			}
			r.close();

			return mesh;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts a standard mesh into OBJ format.
	 *
	 * Vertices that are too close to one another are merged.
	 *
	 * @param data A standard mesh
	 *
	 * @return An OBJ-Formatted mesh
	 */
	public static OBJMesh convertWithMerging(MeshData data) {
		return convert(data, TOLERANCE_POSITION, TOLERANCE_UV, TOLERANCE_NORMAL);
	}

	/**
	 * Converts a standard mesh into OBJ format.
	 *
	 * This method never merges vertices.
	 *
	 * @param data A standard mesh
	 *
	 * @return An OBJ-Formatted mesh
	 */
	public static OBJMesh convert(MeshData data) {
		return convert(data, -1, -1, Float.MAX_VALUE);
	}
	
	/**
	 * Converts a standard mesh into OBJ format.
	 *
	 * Vertices that are too close to one another are merged.
	 *
	 * @param data 		A standard mesh
	 * @param tPosSq	Tolerance to identify positions
	 * @param tUVSq		Tolerance to identify texture coordinates
	 * @param tNormDot	Tolerance to identify normals
	 *
	 * @return An OBJ-Formatted mesh
	 */
	public static OBJMesh convert(MeshData data, float tPosSq, float tUVSq, float tNormDot) {
		if(data == null || !data.hasData()) return null;

		OBJMesh mesh = new OBJMesh();
		Vector3 v3;
		Vector2 v2;
		
		// For minifying the mesh
		int[] posInds = null, uvInds = null, normInds = null;
		PosComparer cmpPos = new PosComparer(tPosSq);
		UVComparer cmpUV = new UVComparer(tUVSq);
		NormComparer cmpNorm = new NormComparer(tNormDot);		
		
		// Hash positions
		posInds = new int[data.vertexCount];
		posInds[0] = 0;
		mesh.positions.add(new Vector3(
			data.positions.get(0),	
			data.positions.get(1),	
			data.positions.get(2)
			));
		for(int i = 1;i < posInds.length;i++) {
			// Extract Position
			v3 = new Vector3(
				data.positions.get(i * 3),
				data.positions.get(i * 3 + 1),
				data.positions.get(i * 3 + 2)
				);
			posInds[i] = indexOfUnique(mesh.positions, v3, cmpPos);
		}
		
		// Hash normals
		if(data.hasNormals()) {
			normInds = new int[data.vertexCount];
			normInds[0] = 0;
			mesh.normals.add(new Vector3(
				data.normals.get(0),	
				data.normals.get(1),	
				data.normals.get(2)
				));
			for(int i = 1;i < normInds.length;i++) {
				// Extract Position
				v3 = new Vector3(
					data.normals.get(i * 3),
					data.normals.get(i * 3 + 1),
					data.normals.get(i * 3 + 2)
					);
				normInds[i] = indexOfUnique(mesh.normals, v3, cmpNorm);
			}
		}
		
		// Hash UVs
		if(data.hasUVs()) {
			uvInds = new int[data.vertexCount];
			uvInds[0] = 0;
			mesh.uvs.add(new Vector2(
				data.uvs.get(0),	
				data.uvs.get(1)	
				));
			for(int i = 1;i < uvInds.length;i++) {
				// Extract Position
				v2 = new Vector2(
					data.uvs.get(i * 2),
					data.uvs.get(i * 2 + 1)
					);
				uvInds[i] = indexOfUnique(mesh.uvs, v2, cmpUV);
			}
		}
		
		// Create vertices and triangles
		HashMap<Vector3i, Integer> vertMap = new HashMap<Vector3i, Integer>();
		for(int i = 0;i < data.indexCount;) {
			Vector3i tri = new Vector3i();
			for(int vi = 0;vi < 3;vi++) {
				Vector3i vert = new Vector3i();
				int vertIndex = data.indices.get(i);
				vert.x = posInds[vertIndex];
				vert.y = (uvInds == null) ? -1 : uvInds[vertIndex];
				vert.z = (normInds == null) ? -1 : normInds[vertIndex];
				i++;
				
				if(vertMap.containsKey(vert)) {
					tri.set(vi, vertMap.get(vert));
				}
				else {
					tri.set(vi, vertMap.size());
					vertMap.put(vert, vertMap.size());
					mesh.vertices.add(vert);
				}
			}
			mesh.triangles.add(tri);
		}
		
		return mesh;
	}
	
	/**
	 * Returns vertex index if it was already added to the mesh. 
	 *
	 * @param arr 	List of vertices currently in mesh
	 * @param obj 	Vertex to add to mesh
	 * @param comp	Equality comparator (including tolerances)
	 *
	 * @return vertex index if already added to the mesh; otherwise -1.
	 */
	private static <T> int indexOfUnique(ArrayList<T> arr, T obj, Comparator<T> comp) {
		// Find if it was already put in
		boolean foundDuplicate = false;
		for(int ii = 0;ii < arr.size() && !foundDuplicate;ii++) {
			if(comp.compare(arr.get(ii), obj) == 0) {
				foundDuplicate = true;
				return ii;
			}
		}
		
		if (!foundDuplicate) {
			// Add the new position
			int ii = arr.size();
			arr.add(obj);
			return ii;
		}
		
		return -1;
	}
	
	/**
	 * Writes an OBJ Mesh out to a file.
	 *
	 * @param w 	An output stream
	 * @param mesh 	A mesh
	 */
	public static void write(PrintWriter w, OBJMesh mesh) {
		if(mesh == null || !mesh.hasData()) return;

		// Write Positions
		for(Vector3 v : mesh.positions) {
			w.write(String.format("v %f %f %f\n", v.x, v.y, v.z));
		}
		// Write Normals
		for(Vector3 v : mesh.normals) {
			w.write(String.format("vn %f %f %f\n", v.x, v.y, v.z));
		}
		// Write UVs
		for(Vector2 v : mesh.uvs) {
			w.write(String.format("vt %f %f\n", v.x, v.y));
		}
		
		// Write Triangles
		if(mesh.hasUVs()) {
			if(mesh.hasNormals()) {
				for(Vector3i t : mesh.triangles) {
					w.write("f");
					for(int v = 0;v < 3; v++) {
						Vector3i vert = mesh.vertices.get(t.get(v));
						w.write(String.format(" %d/%d/%d", vert.x + 1, vert.y + 1, vert.z + 1));
					}
					w.write("\n");
				}
			} else {
				for(Vector3i t : mesh.triangles) {
					w.write("f");
					for(int v = 0;v < 3; v++) {
						Vector3i vert = mesh.vertices.get(t.get(v));
						w.write(String.format(" %d/%d", vert.x + 1, vert.y + 1));
					}
					w.write("\n");
				}
			}
		} else if(mesh.hasNormals()) {
			for(Vector3i t : mesh.triangles) {
				w.write("f");
				for(int v = 0;v < 3; v++) {
					Vector3i vert = mesh.vertices.get(t.get(v));
					w.write(String.format(" %d//%d", vert.x + 1, vert.z + 1));
				}
				w.write("\n");
			}
		} else {
			for(Vector3i t : mesh.triangles) {
				w.write("f");
				for(int v = 0;v < 3; v++) {
					Vector3i vert = mesh.vertices.get(t.get(v));
					w.write(String.format(" %d", vert.x + 1));
				}
				w.write("\n");
			}
		}
	}

	/**
	 * Comparator for vertex positions.
	 *
	 * Two positions are identical if their squared distance is with tolerance.
	 */
	static class PosComparer implements Comparator<Vector3> {
		/** Comparison tolerance */
		private float tolerance;
		
		/** 
		 * Creates a new comparator with the given tolerance.
		 *
		 * @param tolerance The position tolerance
		 */
		public PosComparer(float tolerance) { 
			this.tolerance = tolerance; 
		}
		
		/**
		 * Compares its two arguments for order. Returns a negative integer, zero, 
		 * or a positive integer as the first argument is less than, equal to, or 
		 * greater than the second. 
		 *
		 * This implementation is only used for equality and so only returns 0, 1.
		 *
		 * @param o1	the first object to be compared.
		 * @param o2	the second object to be compared.
		 */
		@Override
		public int compare(Vector3 o1, Vector3 o2) {
			return o1.dst2(o2) <= tolerance ? 0 : 1;
		}
	}

	/**
	 * Comparator for texture coordinates.
	 *
	 * Two texture coordinates are identical if their squared distance is with tolerance.
	 */
	static class UVComparer implements Comparator<Vector2> {
		/** Comparison tolerance */
		private float tolerance;
		
		/** 
		 * Creates a new comparator with the given tolerance.
		 *
		 * @param tolerance The texture coordinate tolerance
		 */
		public UVComparer(float tolerance) { 
			this.tolerance = tolerance; 
		}

		/**
		 * Compares its two arguments for order. Returns a negative integer, zero, 
		 * or a positive integer as the first argument is less than, equal to, or 
		 * greater than the second. 
		 *
		 * This implementation is only used for equality and so only returns 0, 1.
		 *
		 * @param o1	the first object to be compared.
		 * @param o2	the second object to be compared.
		 */
		@Override
		public int compare(Vector2 o1, Vector2 o2) {
			return o1.dst2(o2) <= tolerance ? 0 : 1;
		}
	}
	
	/**
	 * Comparator for vertex normals.
	 *
	 * Two vertex normals coordinates are identical if their dot product is outside
	 * of the tolerance.
	 */	
	static class NormComparer implements Comparator<Vector3> {
		/** Comparison tolerance */
		private float tolerance;
		
		/** 
		 * Creates a new comparator with the given tolerance.
		 *
		 * @param tolerance The vertex normal tolerance
		 */
		public NormComparer(float tolerance) { 
			this.tolerance = tolerance; 
		}

		/**
		 * Compares its two arguments for order. Returns a negative integer, zero, 
		 * or a positive integer as the first argument is less than, equal to, or 
		 * greater than the second. 
		 *
		 * This implementation is only used for equality and so only returns 0, 1.
		 *
		 * @param o1	the first object to be compared.
		 * @param o2	the second object to be compared.
		 */
		@Override
		public int compare(Vector3 o1, Vector3 o2) {
			return o1.dot(o2) >= tolerance ? 0 : 1;
		}
	}
}