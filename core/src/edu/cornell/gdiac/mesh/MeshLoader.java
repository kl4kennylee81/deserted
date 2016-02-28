/*
 * MeshLoader.java
 *
 * It is possible to extend AssetManager to support new asset types.  You just have to
 * define a loader like this one.  See the instructions in the documentation:
 *
 * https://github.com/libgdx/libgdx/wiki/Managing-your-assets
 *
 * Author: Cristian Zaloj
 * Modified by Walker White to support LibGDX AssetManager, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

/**
 * Loader to import OBJ mesh files.
 *
 * The vertices are loaded asynchronously.  However, LibGDX requires that a Mesh object
 * be created from a valid OpenGL context.  Therefore, we do not create the mesh until
 * the final, synchronous step.
 */
public class MeshLoader extends AsynchronousAssetLoader<Mesh, MeshLoader.MeshParameter> {
	
	/** The number of vertices in this mesh */
	private int vertexCount;
	/** The interlaced (position, texture, normal) vertex data */
	private float[] interlacedVerts;
	/** The vertex indices structuring the mesh */
	private short[] interlacedInds;

	/**
	 * Creates a new MeshLoader with the given resolver
	 *
	 * @param resolver the rules for resolving file handles
	 */
	public MeshLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	/** 
	 * Loads the non-OpenGL part of the asset and injects any dependencies of the asset 
	 * into the AssetManager. This part of the implementation is executed in a separate
	 * thread, allowing us to monitor progress.
	 *
	 * @param manager The asset manager directing the loader
	 * @param fileName the name of the asset to load
	 * @param file the resolved file to load
	 * @param parameter the parameters to use for loading the asset
	 */
	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, MeshParameter parameter) {
		MeshDataInterlaced md = OBJParser.parse(file.read(), false, false, -1, -1, 2).interlace(true);
		vertexCount = md.vertexCount;
		
		// Interlace Vertices
	 	interlacedVerts = new float[md.vertexCount * md.vertexComponentCount];
	 	int ii = 0;
	 	for(int i = 0;i < interlacedVerts.length;i++) {
	 		interlacedVerts[ii++] = md.vertices.get(i);
	 	}

	 	// Interlace Indices
	 	interlacedInds = new short[md.indexCount];
	 	ii = 0;
	 	for(int i = 0;i < md.indexCount;i++) {
	 		interlacedInds[ii++] = (short)md.indices.get(i);
	 	}
	}

	/** 
	 * Loads the OpenGL part of the asset.
	 * 
	 * This method is called after asynchronous loading, to create the final object.
	 *
	 * @param manager The asset manager directing the loader
	 * @param fileName the name of the asset to load
	 * @param file the resolved file to load
	 * @param parameter the parameters to use for loading the asset
	 *
	 * @return the loaded asset (or null if loading failed)
	 */
	@Override
	public Mesh loadSync(AssetManager manager, String fileName, FileHandle file, MeshParameter parameter) {
		VertexAttribute[] attribs;
		if (parameter == null || parameter.attribs == null) {
			attribs = new VertexAttribute[2];
			attribs[0] = new VertexAttribute(Usage.Position, 3, "vPosition");
			attribs[1] = new VertexAttribute(Usage.TextureCoordinates, 2, "vUV");
		} else {
			attribs = parameter.attribs;
		}
		
		Mesh mesh = new Mesh(true, vertexCount, interlacedInds.length, attribs);
	 	mesh.setVertices(interlacedVerts);
	 	mesh.setIndices(interlacedInds);
	 	
	 	interlacedInds = null;
	 	interlacedVerts = null;
		return mesh;
	}
	
	/** 
	 * Returns the assets this asset requires to be loaded first.
	 *
	 * This method may be called on a thread other than the GL thread.
	 *	 
	 * @param manager The asset manager directing the loader
	 * @param fileName the name of the asset to load
	 * @param file the resolved file to load
	 * @param parameter the parameters to use for loading the asset
	 *
	 * @return the assets this asset requires to be loaded first.
	 */
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, MeshParameter parameter) {
		return null;
	}

	/** The vertex attribute parameter for constructing a mesh from interleaved data */
	static public class MeshParameter extends AssetLoaderParameters<Mesh> {
		/** The list of vertex attributes for this mesh */
		public VertexAttribute[] attribs = null;
	}
}