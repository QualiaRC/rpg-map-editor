package me.ryan_clark.rpg_map_editor;

import java.io.Serializable;

public class Tile implements Serializable {

	private static final long serialVersionUID = 3740910742090842301L;
	
	public final int spriteId;
	public final int collisionId;
	
	public Tile(int spriteId, int collisionId) {
		this.spriteId = spriteId;
		this.collisionId = collisionId;
	}
	
	public Tile(Tile t) {
		this.spriteId = t.spriteId;
		this.collisionId = t.collisionId;
	}
}
