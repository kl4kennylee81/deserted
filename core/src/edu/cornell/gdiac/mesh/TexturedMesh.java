/*
 * TexturedMesh.java
 * 
 * This class is provided as a convenience, since we will always pair textures and 
 * meshes together (e.g. there are no wireframes).
 * 
 * Author: Walker M. White
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.mesh;

import com.badlogic.gdx.graphics.*;

/**
 * Struct to gather texture, mesh, and color data in one place.
 * 
 * We assume that color can be altered, but that texture and mesh data will not be
 * altered without making a new object.
 *
 * This struct DOES not obtain ownership of its children, and is therefore not
 * responsible for disposing either the texture or mesh.  That is the responsibility
 * of the owner of this object.
 */
public class TexturedMesh {

	/** The 3D geometry information */
	private Mesh mesh;
	/** The texture used by the mesh */
	private Texture texture;
	/** The (current) texture color */
	private Color color;

	/** 
	 * Creates a new TexturedMesh with the given geometry but no texture.
	 *
	 * The initial color is white
	 *
	 * @param mesh 		The 3D geometry information
	 */
	public TexturedMesh(Mesh mesh) {
		this.mesh = mesh;
		this.texture = null;
		color = new Color(Color.WHITE);
	}
	
	/** 
	 * Creates a new TexturedMesh with the given geometry and texture.
	 *
	 * The initial color is white
	 *
	 * @param mesh 		The 3D geometry information
	 * @param texture	The texture used by the mesh
	 */
	public TexturedMesh(Mesh mesh, Texture texture) {
		this.mesh = mesh;
		this.texture = texture;
		color = Color.WHITE;
	}
	
	/**
	 * Returns the mesh with the 3D geometry information
	 *
	 * @return the 3D geometry information
	 */
	public Mesh getMesh() {
		return mesh;
	}

	/**
	 * Returns the texture used by the mesh
	 *
	 * @return the texture used by the mesh
	 */
	public Texture getTexture() {
		return texture;
	}
	
	/**
	 * Sets the texture used by the mesh
	 *
	 * @param texture the texture used by the mesh
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	/**
	 * Returns the color to tint the texture
	 *
	 * @return the color to tint the texture
	 */
	 public Color getColor() {
	 	return color;
	 }
	 
	/**
	 * Sets the color to tint the texture
	 *
	 * @param color the color to tint the texture
	 */
	 public void setColor(Color color) {
	 	this.color = color;
	 }
	 
}